import type { NextConfig } from "next";

const nextConfig: NextConfig = {
	images: {
		remotePatterns: [new URL("https://s4.anilist.co/file/anilistcdn/media/**/*.*"), new URL("https://image.tmdb.org/t/p/**/*.*")],
	},
	turbopack: {
		root: __dirname,
	},
	reactStrictMode: true,
	output: "standalone",
};

export default nextConfig;
