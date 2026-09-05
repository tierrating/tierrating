"use client";

import TierList from "@/app/user/[username]/[service]/[type]/_components/tier-list";
import TmdbDisclaimer from "@/components/disclaimer/tmdb-disclaimer";
import { useState } from "react";
import { Toggle } from "@/components/ui/toggle";
import { ArrowLeftFromLine, ArrowRightFromLine, ArrowDownFromLine, ArrowUpFromLine } from "lucide-react";
import { cn } from "@/lib/utils";
import { ButtonGroup } from "@/components/ui/button-group";
import { useAuth } from "@/contexts/auth-context";
import { useTierlistEntries } from "@/lib/services/media-service";
import { runSync } from "@/lib/services/sync-service";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { toast } from "sonner";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";

export default function TierListPage({
	title,
	username,
	service,
	type,
}: {
	title: string;
	username: string;
	service: string;
	type: string;
}) {
	const { user, token } = useAuth();
	const [isFullWidth, setIsFullWidth] = useState<boolean>(false);
	const [isPullRunning, setIsPullRunning] = useState<boolean>(false);

	const modificationEnabled: boolean = user == username;

	const { mutate: entriesMutate } = useTierlistEntries(username, service, type, token!);

	const pullUpdate = () => {
		setIsPullRunning(true);
		runSync(service, type, token!)
			.then(() => entriesMutate())
			.catch((error) => toast.error(error.message))
			.finally(() => setIsPullRunning(false));
	};

	const pullText = isPullRunning ? "Pulling" : "Pull";
	const PullIcon = isPullRunning ? Spinner : ArrowDownFromLine;

	return (
		<div className={cn("max-w-full px-4")}>
			<div className={"grid grid-cols-2 2xl:w-[1514px] m-auto"}>
				<h1 className="text-3xl font-bold mb-6">{title}</h1>
				<div className={"w-full flex justify-end items-end pb-2"}>
					<ButtonGroup>
						{modificationEnabled && (
							<ButtonGroup>
								<Tooltip>
									<TooltipTrigger asChild>
										<Button variant="outline" disabled aria-label="Push">
											<ArrowUpFromLine data-icon="inline-start" />
											Push
										</Button>
									</TooltipTrigger>
									<TooltipContent>Pushing data to third-party services is not available yet.</TooltipContent>
								</Tooltip>

								<Tooltip>
									<TooltipTrigger asChild>
										<Button variant="outline" disabled={isPullRunning} onClick={pullUpdate} aria-label={pullText}>
											<PullIcon data-icon="inline-start" />
											{pullText}
										</Button>
									</TooltipTrigger>
									<TooltipContent>
										Pulling data from third-party service adding new entries and overwriting local scores.
									</TooltipContent>
								</Tooltip>
							</ButtonGroup>
						)}
						<ButtonGroup>
							<Toggle variant="outline" aria-label={"Toggle full width"} onPressedChange={() => setIsFullWidth(!isFullWidth)}>
								<ArrowLeftFromLine />
								Full Width
								<ArrowRightFromLine />
							</Toggle>
						</ButtonGroup>
					</ButtonGroup>
				</div>
			</div>
			<div className={cn("m-auto transition-all duration-400 ease-in-out", isFullWidth ? "w-full" : "2xl:w-[1514px]")}>
				<TierList username={username} service={service} type={type} modificationEnabled={modificationEnabled && !isPullRunning} />
			</div>
			{service.startsWith("trakt") && <TmdbDisclaimer />}
		</div>
	);
}
