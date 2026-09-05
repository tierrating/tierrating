"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "@/contexts/auth-context";
import { LoadingPage } from "@/components/loading-skeletons/loading-page";

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
	const { isAuthenticated, isLoading, isExpired } = useAuth();
	const router = useRouter();

	useEffect(() => {
		if (!isLoading && (!isAuthenticated || isExpired)) {
			router.push("/login");
		}
	}, [isAuthenticated, isLoading, isExpired, router]);

	if (isLoading || !isAuthenticated || (!isLoading && (!isAuthenticated || isExpired))) {
		return <LoadingPage />;
	}

	return <>{children}</>;
}

export function AnonymousAllowedRoute({ children }: { children: React.ReactNode }) {
	const { user, isAuthenticated, isLoading } = useAuth();
	const router = useRouter();
	const currentPath = usePathname();

	useEffect(() => {
		if (!isLoading && isAuthenticated && (currentPath == "/login" || currentPath == "/signup")) {
			router.push(`/user/${user}`);
		}
	}, [user, isAuthenticated, isLoading, router, currentPath]);

	if (isLoading || (!isLoading && isAuthenticated && (currentPath == "/login" || currentPath == "/signup"))) {
		return <LoadingPage />;
	}

	return <>{children}</>;
}

export function RestrictedRenderingRoute({ children }: { children: React.ReactNode }) {
	const pathname = usePathname();

	if (pathname === "/login" || pathname === "/signup") {
		return null;
	}

	return <>{children}</>;
}
