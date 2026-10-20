# Recon Map: Spotify Mobile (Android)

Scope: Core music streaming, search & browse, personal library, queue management, and interactive full-screen audio player.
For: Spotify high-fidelity clean-room clone.
Date: 2026-10-19

## Sources

| # | source | URL | notes |
| --- | --- | --- | --- |
| 1 | Spotify Android Client | public app specifications | Mobile UI & navigation architecture |
| 2 | Spotify Developer API Docs | developer.spotify.com | Schema, audio playback models, playlist endpoints |
| 3 | Material 3 Dark Guidelines | m3.material.io | Dark-mode media controls and contrast accessibility |

## Core Loop

Discovering music, browsing genres/curated mixes, creating and managing personal playlists, and streaming audio with seamless queue and playback controls.

## Screens

| ID | screen | route / how to reach | purpose | key components | states seen |
| --- | --- | --- | --- | --- | --- |
| S01 | Home Feed | bottom nav 'Home' | Personalized feed of playlists, mixes, podcasts, and recent history | Category chips (All/Music/Podcasts), quick-play grid, horizontal carousels, cards | loaded, filtered, refreshed |
| S02 | Search & Browse | bottom nav 'Search' | Search tracks, artists, albums, or explore colored genre cards | Search text field with clear button, genre grid cards, live search results list | initial browse, searching, empty results |
| S03 | Your Library | bottom nav 'Your Library' | User playlists, artists, albums, and liked songs collection | Filter chips, pinned 'Liked Songs' card, playlist list/grid toggle, create playlist modal | populated, filtered, empty custom playlist |
| S04 | Playlist Detail | click playlist card | View playlist metadata, track list, play/shuffle controls | Gradient header, cover art, play button, track items with duration & actions | loaded, playing track highlighted |
| S05 | Mini Player | persistent bar above bottom nav | Quick playback glance & controls while browsing | Album art thumbnail, title, artist marquee, play/pause toggle, like button, progress bar | visible when playing/paused |
| S06 | Full Player | tap/expand Mini Player | Immersive audio controls, lyrics, queue, shuffle/repeat | Dominant color gradient, album art, seek slider, timestamp labels, playback controls | playing, paused, seeking, lyrics open, queue open |
| S07 | Queue Drawer | tap queue icon on Full Player | Manage and inspect upcoming tracks | Currently playing header, upcoming list, remove/reorder actions | populated queue |

## Flows

```
F01 Quick Stream from Home
    S01 (Home) -> Tap Play on "Daily Mix 1" -> S05 (Mini Player appears) -> Tap Mini Player -> S06 (Full Player)
    happy path clicks: 2

F02 Search & Discover
    S02 (Search) -> Type "Midnight" -> Select Track -> Audio plays -> Added to history
    happy path clicks: 2

F03 Playlist Creation & Song Saving
    S03 (Library) -> Tap "+" -> Name Playlist -> Save -> Open Track -> "Add to Playlist" -> Track saved
    happy path clicks: 4
```

## Components

| component | variants | states | used on |
| --- | --- | --- | --- |
| BottomNavigation | 3 tabs (Home, Search, Your Library) | active, inactive | app shell |
| MiniPlayer | collapsed bar | playing, paused, dragging | bottom overlay |
| TrackItem | list item with index, art, title, artist, menu | normal, currently playing, liked | playlist, search, queue |
| CategoryCard | colorful gradient card with rotated art preview | normal, pressed | search |
| PlaybackSlider | scrubbable progress bar | idle, scrubbing | player |
| PlayFab | large green round play/pause button | playing, paused | playlist header, player |

## Inferred Data Model

```
Track: id, title, artist, album, durationMs, coverUrl, audioUrl, lyrics, genres, isLiked
Playlist: id, name, description, coverUrl, isCustom, trackIds, owner, colorHex
GenreCategory: id, title, colorHex, icon
PlaybackState: currentTrack, isPlaying, positionMs, durationMs, shuffle, repeatMode, queue
```

## Feature Matrix

See `replica/features.csv`. Must: 8, should: 4, could: 2.
