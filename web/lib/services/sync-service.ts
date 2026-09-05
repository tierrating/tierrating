import { apiClient } from "@/lib/api-client";
import { useApiMutation } from "@/lib/use-api";
import { ApiRequestError } from "@/types/api-request-error";

export interface SyncStatus {
	source?: string;
	type?: string;
	status?: "PENDING" | "IN_PROGRESS" | "COMPLETED" | "FAILED";
	startedAt?: string;
}

export function useSyncEnqueue(service: string, type: string, token: string) {
	return useApiMutation<void, void>(`/sync/${service}/${type}`, { token });
}

/**
 * Enqueues a sync for the current user and waits until it finishes (or the timeout is reached).
 * A 409 response means a sync is already queued - the wait continues in that case.
 */
export async function runSync(service: string, type: string, token: string, timeoutMs = 5 * 60 * 1000): Promise<void> {
	try {
		await apiClient<void>(`/sync/${service}/${type}`, { method: "POST", token });
	} catch (error) {
		if (!(error instanceof ApiRequestError && error.status === 409)) throw error;
	}

	const deadline = Date.now() + timeoutMs;
	while (Date.now() < deadline) {
		const status = await apiClient<SyncStatus>(`/sync/status/${service}/${type}`, { token });
		if (status.status === undefined || (status.status !== "PENDING" && status.status !== "IN_PROGRESS")) {
			if (status.status === "FAILED") throw new Error("Sync failed. Please try again later.");
			return;
		}
		await new Promise((resolve) => setTimeout(resolve, 2000));
	}
	throw new Error("Sync is taking unusually long. Check back in a moment.");
}
