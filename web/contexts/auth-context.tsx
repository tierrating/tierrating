"use client";

import { createContext, ReactNode, useCallback, useContext, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { extractJwtData } from "@/lib/auth/jwt-decoder";
import { useRefreshToken } from "@/lib/services/auth-service";
import { toast } from "sonner";

interface AuthContextType {
	token: string | null;
	user: string | null;
	isLoading: boolean;
	isAuthenticated: boolean;
	isExpired: boolean;
	expiration: Date | null;
	login: (token: string) => void;
	logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
	const [token, setToken] = useState<string | null>(null);

	const [user, setUser] = useState<string | null>(null);
	const [expiration, setExpiration] = useState<Date | null>(null);

	const [isAuthenticated, setIsAuthenticated] = useState(false);
	const [isExpired, setIsExpired] = useState(false);

	const [isLoading, setLoading] = useState(true);
	const router = useRouter();

	const { trigger: refreshToken, error, isMutating: isRefreshingToken } = useRefreshToken();

	const login = useCallback((newToken: string) => {
		localStorage.setItem("authToken", newToken);
		setToken(newToken);
		const extracted = extractJwtData(newToken);
		if (extracted) {
			setUser(extracted.username);
			setExpiration(extracted.expiration);
			setIsExpired(extracted.isExpired);
		}
		setIsAuthenticated(true);
	}, []);

	const logout = useCallback(() => {
		localStorage.removeItem("authToken");
		setToken(null);
		setUser(null);
		setIsAuthenticated(false);
		router.push("/login");
	}, [router]);

	// Load token from localStorage on initial render
	useEffect(() => {
		const checkAuth = () => {
			const storedToken = localStorage.getItem("authToken");
			if (!storedToken) {
				logout();
				setLoading(false);
				return;
			}

			const decodedJwt = extractJwtData(storedToken);
			if (!decodedJwt || decodedJwt.isExpired) {
				logout();
				setLoading(false);
				return;
			}

			setToken(storedToken);
			setUser(decodedJwt.username);
			setIsExpired(decodedJwt.isExpired);
			setExpiration(decodedJwt.expiration);
			setIsAuthenticated(true);

			setLoading(false);
		};
		checkAuth();
	}, [login, logout, refreshToken]);

	// Proactively refresh the token while the page is open so long sessions don't break
	const refreshInFlight = useRef(false);
	useEffect(() => {
		if (!isAuthenticated || !token || !expiration) return;

		const refreshIfExpiringSoon = () => {
			if (refreshInFlight.current) return;
			if (expiration.getTime() - Date.now() > 15 * 60 * 1000) return;

			refreshInFlight.current = true;
			refreshToken({ token })
				.then((response) => {
					refreshInFlight.current = false;
					login(response.token);
				})
				.catch(() => {
					refreshInFlight.current = false;
					toast.error("Error refreshing token");
				});
		};

		refreshIfExpiringSoon();
		const interval = setInterval(refreshIfExpiringSoon, 60 * 1000);
		return () => clearInterval(interval);
	}, [isAuthenticated, token, expiration, refreshToken, login]);

	return (
		<AuthContext.Provider value={{ token, user, isAuthenticated, isLoading, isExpired, expiration, login, logout }}>
			{children}
		</AuthContext.Provider>
	);
}

export function useAuth() {
	const context = useContext(AuthContext);
	if (context === undefined) {
		throw new Error("useAuth must be used within an AuthProvider");
	}
	return context;
}
