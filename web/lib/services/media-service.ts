import { TierlistEntry } from "@/types/types";
import { useApi, useApiMutation } from "@/lib/use-api";

export function useTierlistEntries(username: string, service: string, type: string, token?: string) {
	return useApi<TierlistEntry[]>(`/media/${username}/${service}/${type}`, { token });
}

export function useScoreMutation(username: string, service: string, type: string, token: string) {
	return useApiMutation<void, { id: string; score: number; state: string }>(`/media/${username}/${service}/${type}/update`, { token });
}
