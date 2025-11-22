# TierRating
[![Server Release Build](https://github.com/tierrating/tierrating/actions/workflows/release-build.yml/badge.svg)](https://github.com/tierrating/tierrating/actions/workflows/release-build.yml)
[![UI Release Build](https://github.com/tierrating/tierrating-ui/actions/workflows/release-build.yml/badge.svg)](https://github.com/tierrating/tierrating-ui/actions/workflows/release-build.yml)


Fetch ratings from third-party providers like Anilist and organize them in tier lists. Changes will be synced back to the provider.

## Overview
* Import your ratings from Anilist and Trakt
* View and organize your content in tier lists
* Sync rating changes back to the original platform
* Fully selfhostable

## Screenshots

![anilist-anime-tierlist](screenshots/anilist-anime-tierlist.png)
You can find more screenshots [here](https://github.com/tierrating/tierrating/tree/master/screenshots).

## Installation

Installation guide is available at [tierrating docs](https://docs.tierrating.de/docs/installation/).

## Development

This repository contains the backend service for TierRating, a Spring Boot application that handles fetching and synchronizing ratings with third-party providers.

### Prerequisites

- Java JDK 21 or higher
- Maven 3.6+

### Local Development Setup

1. Clone the repository
```bash
git clone https://github.com/tierrating/tierrating.git
cd tierrating
```

2. Build the application
```bash
mvn clean package install
```

3. Run the application
```bash
mvn spring-boot:run
```

The API will be available at http://localhost:8080

#### Docker Build (Optional)

```bash
docker build -t tierrating:latest .
```
