"use client";

import { useAuth } from "@/contexts/auth-context";
import { useState } from "react";
import { LoadingDiv } from "@/components/loading-skeletons/loading-page";
import ThirdPartyLoginButton from "@/app/settings/_components/third-party-login-button";
import ThirdPartyConnection from "@/app/settings/_components/third-party-connection";
import { useRemoveThirdPartyService, useUser } from "@/lib/services/user-service";
import { getServiceConfig, ThirdPartyServiceConfig } from "@/lib/config/third-party-services-config";
import { toast } from "sonner";
import { useThirdPartyServices } from "@/lib/services/third-party-info-service";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircleIcon } from "lucide-react";

export default function ThirdPartyConfig() {
	const { user, token, logout } = useAuth();
	const [isRemovingService, setIsRemovingService] = useState(false);
	const { data: userData, error: userError, isValidating: isValidatingUser, mutate: reloadUser } = useUser(user!, token!);
	const { data: services, error: servicesError, isValidating: isValidatingService } = useThirdPartyServices(token!);
	const { trigger: removeConnection, error: removalError, isMutating } = useRemoveThirdPartyService(user!, token!);
	const isValidating: boolean = (isValidatingUser || isValidatingService) && (!userData || !services);

	const removeService = (service: string) => {
		removeConnection({ service })
			.then(() => {
				reloadUser();
			})
			.catch((error) => {
				toast.error(`Error occurred deleting ${service} connection. Please try again later.`);
			});
	};

	if (isValidating) return <LoadingDiv className={"min-h-30"} />;

	if (userError || servicesError)
		return (
			<Alert variant="destructive" className="max-w-md">
				<AlertCircleIcon />
				<AlertTitle>Error occured</AlertTitle>
				<AlertDescription>Can&#39;t load configured third-party services. Please try again later.</AlertDescription>
			</Alert>
		);

	const connectedServices: ThirdPartyServiceConfig[] = userData!.connectedServices.map((service) => getServiceConfig(service)!);
	const availableServices: ThirdPartyServiceConfig[] = services!
		.filter((service) => !userData!.connectedServices.includes(service))
		.map((service) => getServiceConfig(service)!);

	return (
		<div className={"w-full grid gap-2"}>
			{availableServices &&
				availableServices.length > 0 &&
				availableServices.map((service) => (
					<ThirdPartyLoginButton
						key={service.id}
						title={`Connect ${service.name}`}
						path={`/auth/${service.id}`}
						service={service.id}
					/>
				))}
			{connectedServices &&
				connectedServices.length > 0 &&
				connectedServices.map((service) => (
					<ThirdPartyConnection
						key={service.id}
						service={{ id: service.id, title: service.name }}
						types={service.types.map((type) => ({ id: type.id, title: type.name }))}
						removeConnection={removeService}
						isRemovingService={isRemovingService}
						username={user!}
						token={token}
						logout={logout}
					/>
				))}
		</div>
	);
}
