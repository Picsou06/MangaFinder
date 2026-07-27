# MangaFinder

Application Android (Java) de lecture et téléchargement de mangas. Premier projet Android de l'auteur, connecté à une API dédiée ([MangaFinderAPI](https://github.com/Picsou06/MangaFinderAPI)) pour récupérer la liste des mangas et leurs chapitres.

## Fonctionnalités

- Parcourir une liste de mangas via l'API (`Download/Mangas`, `ListAnimeAPI`)
- Télécharger des chapitres (`Download/Chapters`, `DownloadJob`)
- Lire les mangas téléchargés avec un lecteur d'images dédié, zoom compris (`Read/Chapters/MangaViewer`, `ScaleListener`)
- Gestion d'une bibliothèque locale (livres et chapitres lus/téléchargés) via SQLite (`BookLocalDatabase`, `BookDAO`, `ChapterDAO`)
- Écran de paramètres (`SettingsActivity`)

## Stack technique

- Android natif en Java (namespace `fr.picsou.mangafinder`), avec Compose et ViewBinding activés
- SQLite pour le stockage local (favoris/téléchargements)
- Appels réseau vers l'API MangaFinderAPI (`Connector/APIConnector`)

## Installation

Ouvrir le projet dans Android Studio (Gradle) et lancer sur un émulateur ou un appareil réel. Nécessite qu'une instance de [MangaFinderAPI](https://github.com/Picsou06/MangaFinderAPI) soit accessible pour la récupération des données.
