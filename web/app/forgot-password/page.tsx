import Link from "next/link";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { AnonymousAllowedRoute } from "@/contexts/route-accessibility";
import { cn } from "@/lib/utils";

export default function ForgotPassword() {
	return (
		<AnonymousAllowedRoute>
			<div className="flex min-h-screen -mt-24 items-center justify-center px-4">
				<Card className={cn("w-full max-w-md z-50", "bg-card/60 backdrop-blur-sm border border-border/100 shadow-lg")}>
					<CardHeader className="space-y-1">
						<CardTitle className="text-2xl font-bold">Forgot password?</CardTitle>
						<CardDescription>Password reset via email is not available yet</CardDescription>
					</CardHeader>
					<CardContent className="text-sm text-muted-foreground">
						This instance does not offer self-service password resets. Please contact the server administrator who runs your
						TierRating instance to reset your password.
					</CardContent>
					<CardFooter className="flex flex-col space-y-4 pt-4">
						<div className="text-center text-sm">
							<Link href="/login" className="text-primary hover:underline">
								Back to login
							</Link>
						</div>
					</CardFooter>
				</Card>
			</div>
		</AnonymousAllowedRoute>
	);
}
