# s2udy - Original App Design Project

## Table of Contents
1. [Overview](#Overview)
1. [Product Specs](#Product-Specs)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview

### Descriptions
#### Study Together
- Create public/private rooms to study together; whoever leaves the app puts a strike on the session (three strikes type thing); includes timers, chat, music, task list, etc.
- Less useful, but might be good for quarantine time

### App Evaluations
#### Study Together
- <u>Category:</u>  Mobile; Social; Productivity
- <u>Mobile:</u>  Keeps users off their phone while studying. 
- <u>Story:</u>  Allows users to study together remotely and keep accountability.
- <u>Market:</u>  Anyone who likes studying in a group setting. Friends who cannot get together but want to study together.
- <u>Habit:</u>  Users can use this app any time they want to study or work, or to join a friend in their work.
- <u>Scope:</u>  Initial version would include public rooms for people to host/join, with to-do list, music player, timers, user chat, etc. Further updates would include private rooms, shareable links to rooms, and personalized features (having your own to-do list/timer).


## Product Specs
### Study Together
    
   1. User Stories (Required and Optional)
        * Required Must-have Stories
            - [x] User can create account
            - [x] User can log in
            - [x] User can create study room
            - [ ] User can join study room
            - [ ] User can edit task list
            - [ ] User can set timer
            - [ ] User can chat
            - [ ] User can play music

        * Optional Nice-to-have Stories
            - [ ] User can create private rooms
            - [ ] User can share rooms through invitation links
            - [ ] User can have personal task list and timer
            - [ ] User can upload and edit a profile picture
            

   2. Screen Archetypes
        * Registration screen
            - User can create account
        * Login screen
            - User can log in
        * Create room screen
            - User can create study room
        * Rooms screen
            - User can join study room
        * Home room screen
            - User can edit task list
            - User can set timer
            - User can chat
            - User can play music
        * Tasklist screen
            - User can edit task list
        * Timer screen
            - User can set tier
        * Chat screen
            - User can chat
        * Music screen
            - User can play music


   3. Navigation
        * Tab Navigation (Tab to Screen)
            - Create room screen
            - Tasklist screen
            - Timer screen
            - Chat screen
            - Music screen

        * Flow Navigation (Screen to Screen)
            - Registration screen
                --> Login screen
                --> Rooms screen
            - Login screen
                --> Registration screen
                --> Rooms screen
            - Create room screen
                --> Home room screen
                --> Rooms screen (cancel creation)
            - Rooms screen
                --> Create room screen
                --> Home room screen (upon joining)
            - Home room screen
                --> Tasklist screen
                --> Timer screen
                --> Chat screen
                --> Music screen
            - Tasklist, Timer, Chat, Music screens
                --> Home room screen 


## Wireframes
### [BONUS] Digital Wireframes & Mockups
<img src="FBU APP.png" width=600>

### [BONUS] Interactive Prototype
Pending...

## Schema 
### Models
#### - Model : User
| Property | Type | Description |
| -------- | ---- | ----------- |
| objectId | string | unique ID pointing to user |
| username | string | unique username |
| password | string | user's password |
| email | string | email used to sign up |
| profilePic | File | profile pic associated with user |

#### - Model: Room
| Property | Type | Description |
| -------- | ---- | ----------- |
| objectID | String | unique ID pointing to room |
| host | Pointer to User | user hosting the room |
| roomName | String | unique room name |
| roomDescr | String | short optional description of room; ie subject, purpose |
| membersCap | Number | member capacity of room |
| membersCount | Number | members in room |
| password | String | (later feature) passcode to enter private room |
| chatEnabled | Boolean | whether or not chat is enabled |
| linkMusic | String | link to music playlist |

### Networking
| CRUD | HTTP VERB | EXAMPLE |
| ---- | --------- | ------- |
| Create | `POST` | Creating a new room |
| Read | `GET` | Fetching rooms for joining |
| Update | `PUT` | Changing a user's profile image |
| Destroy | `DELETE` | Deleting a room |

- JOIN ROOMS SCREEN
    - Query`(GET)` all existing rooms
- IN ROOM SCREEN
    - Query`(GET)` current room object
    - Update`(PUT)` room settings
    - Create`(POST)` new member part of room
- CREATE ROOM SCREEN
    - Create`(POST)` new room
- PROFILE SCREEN
    - Query`(GET)` current user object
    - Update`(PUT)` user profile photo, username, password, email
- [OPTIONAL: List endpoints if using existing API]
    - Spotify:
        - Getting tracks based on ID: `GET` https://api.spotify.com/v1/tracks/
        - Getting playlist from a user: `GET` https://api.spotify.com/v1/playlists/{playlist_id}/tracks
        - Getting an artist's top tracks: `GET` https://api.spotify.com/v1/artists/{id}/top-tracks