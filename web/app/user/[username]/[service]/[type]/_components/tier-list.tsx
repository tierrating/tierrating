"use client";

import React, { startTransition, useEffect, useMemo, useState } from "react";
import { Tier, TierlistEntry } from "@/types/types";
import { TierlistEntrySkeleton } from "@/components/loading-skeletons/tier-container-skeleton";
import { TierlistEntryCard, TierlistEntryDraggable } from "@/app/user/[username]/[service]/[type]/_components/tierlist-entry-draggable";
import { DragDropProvider, DragOverlay } from "@dnd-kit/react";
import { assignTiersAndGroupEntriesByTier, groupBySingle, sortByName } from "@/lib/mapper/tier-mapper";
import TierContainerDroppable from "@/app/user/[username]/[service]/[type]/_components/tier-container-droppable";
import { toast } from "sonner";
import { useTiers } from "@/lib/services/tierlist-service";
import { useScoreMutation, useTierlistEntries } from "@/lib/services/media-service";
import { useAuth } from "@/contexts/auth-context";
import { getDefaultTiers } from "@/lib/config/default-tiers";
import { LoadingPage } from "@/components/loading-skeletons/loading-page";

export default function TierList({
	username,
	service,
	type,
	modificationEnabled,
}: {
	username: string;
	service: string;
	type: string;
	modificationEnabled: boolean;
}) {
	const { token, user, logout } = useAuth();

	const { trigger: pushEntryUpdate, error, isMutating } = useScoreMutation(username, service, type, token!);
	const { data: tiersData, error: tiersError, isValidating: tiersIsLoading } = useTiers(username, service, type, token!);
	const {
		data: entriesData,
		error: entriesError,
		mutate: entriesMutate,
		isValidating: entriesIsLoading,
	} = useTierlistEntries(username, service, type, token!);
	const isLoading = tiersIsLoading || entriesIsLoading;
	const tiers = useMemo(() => (tiersData?.length ? tiersData : getDefaultTiers()), [tiersData]);
	const entries = useMemo(() => entriesData ?? [], [entriesData]);

	const tiersByName = useMemo(() => groupBySingle(tiers, (tier) => tier.name), [tiers]);
	const [entriesById, setEntriesById] = useState<Map<string, TierlistEntry>>(new Map());

	useEffect(() => {
		queueMicrotask(() => {
			setEntriesById(groupBySingle(entries, (entry) => entry.id));
		});
	}, [entries]);

	const initialEntriesByTierName = useMemo(() => assignTiersAndGroupEntriesByTier(tiers, entries), [tiers, entries]);
	const [entriesByTierName, setEntriesByTierName] = useState<Map<string, TierlistEntry[]>>(new Map()); // mutated by user
	const mappingCompleted = entriesByTierName.size > 0;

	useEffect(() => {
		queueMicrotask(() => {
			setEntriesByTierName(initialEntriesByTierName);
		});
	}, [initialEntriesByTierName]);

	const onDragEnd = async (event: { canceled: any; operation: { source: any; target: any } }) => {
		if (event.canceled) return;

		if (!entriesByTierName || !tiersByName || !entriesById) {
			toast.error("Error occurred. Please refresh the page!");
			return;
		}

		const { source, target } = event.operation;
		const entryToChange = entriesById.get(source.id)!;
		const sourceTier = entryToChange.tier!;
		const targetTier = tiersByName.get(target.id)!;

		if (!(entryToChange.tier && targetTier.name)) return;
		if (entryToChange.tier === targetTier) return; // entry already in desired tier

		updateEntry(entryToChange, targetTier, sourceTier);

		startTransition(() => {
			// TODO: make state variable
			pushEntryUpdate({ id: entryToChange.id, score: targetTier.adjustedScore, state: entryToChange.state }).catch((error) => {
				toast.error(`Couldn't update ${entryToChange.title}. Reverted change.\n Error: ${error.message}`);
				updateEntry(entryToChange, sourceTier!, targetTier);
			});
		});
	};

	const updateEntry = (entryToChange: TierlistEntry, targetTier: Tier, sourceTier: Tier) => {
		const updatedEntry = {
			...entryToChange,
			tier: targetTier,
			score: targetTier.adjustedScore,
		};

		if (sourceTier.name !== targetTier.name) {
			// update element to avoid stale drag overlay data
			setEntriesById((prev) => new Map(prev).set(updatedEntry.id, updatedEntry));
		}

		setEntriesByTierName((prevMap) => {
			const newMap = new Map(prevMap);
			if (sourceTier.name !== targetTier.name) {
				// add entryToChange to new tier
				const targetEntries = [...newMap.get(targetTier.name)!, updatedEntry].sort(sortByName);
				newMap.set(targetTier.name, targetEntries);
				// remove entryToChange from its current tier
				const updatedEntries = [...newMap.get(sourceTier.name)!.filter((entry) => entry.id !== entryToChange.id)];
				newMap.set(sourceTier.name, updatedEntries);
			}
			return newMap;
		});
	};

	if (isLoading && !entriesData && !tiersData) return <LoadingPage />;

	if (tiersError || entriesError) {
		if (tiersError?.status === 404 || entriesError?.status === 404) {
			return <div>Tierlist of user does not exist or is private.</div>;
		}
		return <div>Error occurred while fetching your data. Please try again later. If the error persists contact the server admin.</div>;
	}

	if (!mappingCompleted) {
		return tiers.map((tier) => <TierlistEntrySkeleton key={tier.name} color={tier.color} label={tier.name} />);
	}

	return (
		<DragDropProvider onDragEnd={onDragEnd}>
			{tiers.map((tier) => (
				<TierContainerDroppable key={tier.name} id={tier.name} label={tier.name} color={tier.color} disabled={!modificationEnabled}>
					{entriesByTierName.get(tier.name)!.map((entry) => (
						<TierlistEntryDraggable key={entry.id} entry={entry} disabled={!modificationEnabled} />
					))}
				</TierContainerDroppable>
			))}
			<DragOverlay>
				{(source) => (
					// @ts-expect-error - Type mismatch in drag overlay source data
					<TierlistEntryCard key={source.id} entry={entriesById.get(source.id)} />
				)}
			</DragOverlay>
		</DragDropProvider>
	);
}
