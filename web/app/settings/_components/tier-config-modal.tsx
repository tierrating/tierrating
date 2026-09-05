"use client";
import * as React from "react";
import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ArrowUpDown, Settings } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { getDefaultTiers } from "@/lib/config/default-tiers";
import { useTiersOnDemand, useTiersUpdate } from "@/lib/services/tierlist-service";
import { toast } from "sonner";
import { EditableTier, TierConfigModalBody } from "@/app/settings/_components/tier-config-modal-body";

interface TierConfigModalProps {
	service: string;
	type: string;
	username: string;
	decimals: string;
}

export default function TierConfigModal({ service, type, username, decimals }: TierConfigModalProps) {
	const { token } = useAuth();
	const [isOpen, setIsOpen] = useState(false);
	const {
		data: tiersData,
		error: tiersError,
		isValidating,
		mutate: refreshTiers,
	} = useTiersOnDemand(!isOpen, username, service, type, token!);

	const [tiers, setTiers] = useState<EditableTier[]>([]);
	useEffect(() => {
		// eslint-disable-next-line react-hooks/set-state-in-effect
		setTiers((tiersData?.length ? tiersData : getDefaultTiers()).map((tier) => ({ ...tier, id: crypto.randomUUID() })));
	}, [tiersData]);

	const { trigger: updateTiers, error, isMutating } = useTiersUpdate(username, service, type, token!);

	const handleSave = () => {
		const sortedTiers = [...tiers].sort((a, b) => b.score - a.score);
		updateTiers({ tiers: sortedTiers.map(({ id: _id, ...tier }) => tier) }).catch((error) => {
			toast.error("Something went wrong while saving tiers.");
		});
		setIsOpen(false);
	};

	const handleOpenChange = (open: boolean) => {
		setTimeout(() => setIsOpen(open), open ? 0 : 200);
	};

	return (
		<Dialog open={isOpen} onOpenChange={handleOpenChange}>
			<DialogTrigger asChild className="p-0 cursor-pointer">
				<Button variant={"outline"}>
					<Settings />
				</Button>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[650px] sm:max-h-[60vh] max-h-[90vh] min-h-0">
				<DialogHeader>
					<DialogTitle>Configure tier list</DialogTitle>
					<DialogDescription>
						Configure which <i>score</i> should be assigned to which tier. When dropping an item into a new tier their score
						will be set to <i>adjusted score</i>.
					</DialogDescription>
					<p className="mt-1 font-medium text-sm flex items-center">
						<ArrowUpDown className="h-3 w-3 mr-1" />
						Tiers are automatically sorted by score in descending order.
					</p>
				</DialogHeader>
				<TierConfigModalBody
					tiers={tiers}
					setTiers={setTiers}
					decimals={decimals}
					isValidating={isValidating}
					isMutating={isMutating}
				/>
				<DialogFooter className="flex gap-2">
					<Button
						variant="secondary"
						onClick={() => setTiers(getDefaultTiers().map((tier) => ({ ...tier, id: crypto.randomUUID() })))}
					>
						Restore defaults
					</Button>
					<Button variant="outline" onClick={() => setIsOpen(false)}>
						Cancel
					</Button>
					<Button onClick={handleSave} disabled={isMutating}>
						Save Changes
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
