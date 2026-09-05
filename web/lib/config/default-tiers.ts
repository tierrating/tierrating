import { Tier } from "@/types/types";

export function getDefaultTiers(): Tier[] {
	return [
		{
			name: "S",
			score: 10.0,
			adjustedScore: 10.0,
			color: "#FF7F7F",
		},
		{
			name: "A+",
			score: 9.0,
			adjustedScore: 9.0,
			color: "#FF9E7F",
		},
		{
			name: "A",
			score: 8.0,
			adjustedScore: 8.0,
			color: "#FFBF7F",
		},
		{
			name: "B+",
			score: 7.0,
			adjustedScore: 7.0,
			color: "#e4e449",
		},
		{
			name: "B",
			score: 6.0,
			adjustedScore: 6.0,
			color: "#aae371",
		},
		{
			name: "C+",
			score: 5.0,
			adjustedScore: 5.0,
			color: "#7FDFBF",
		},
		{
			name: "C",
			score: 4.0,
			adjustedScore: 4.0,
			color: "#7FBFFF",
		},
		{
			name: "D",
			score: 3.0,
			adjustedScore: 3.0,
			color: "#9F7FFF",
		},
		{
			name: "E",
			score: 2.0,
			adjustedScore: 2.0,
			color: "#BF7FFF",
		},
		{
			name: "F",
			score: 1.0,
			adjustedScore: 1.0,
			color: "#FF7FBF",
		},
		{
			name: "-",
			score: 0.0,
			adjustedScore: 0.0,
			color: "#E6E6FF",
		},
	];
}
