# Study Mate Android App

## Overview

This Android application is designed to enhance your study experience, offering a range of features to support your academic journey. Before diving in, ensure your Android device is compatible, and follow the steps below to set up the app.

## Getting Started

1. **Permissions:** When opening the app, grant location permissions for optimal functionality.
2. **Background Permissions:** If prompted, allow background permissions to enable continuous operation.

## Project Structure

The project encompasses various components organized into packages for clarity and maintainability. Here's a breakdown of key modules:

### com.example.studymate

This package houses all the activities, fragments, and more.

- **MainActivity:** Manages fragments, fetches data from Firebase, and handles Geofence logic.
- **MapsFragment:** A generic fragment for loading the Google Maps SDK.
- **AuthenticationActivity:** Manages user login, register, and authentication fragments.

### com.example.studymate.data.model

Contains data structures for the app.

- **ApplicationState:** Stores live data for the logged-in user.
- **Course:** Data model for Firebase courses.
- **File:** Custom model for storing user files.
- **StudyGroup:** Custom class for study groups.
- **User:** Custom class for Firebase User collection.
- **StudySpot:** Geofenced study locations from Firebase.

### com.example.studymate.services

Includes services and receivers.

- **CloseService:** Custom service triggered when the app is closed, updating Firebase Study Spots.
- **GeofenceBroadCastReceiver:** Receiver handling geofence events.
- **MyAppGlidemodule:** Custom component using the Glide package to load images.

### com.example.studymate.ui

Focuses on UI-related activities and fragments.

- **LoadingDialog:** Custom component for displaying a loading spinner during async activities.

### com.example.studymate.ui.login

Manages the login fragment for the AuthActivity.

- **LoginFormState:** Manages errors in the LoginFragment views.
- **LoginFragment:** Fragment tied to the Login view and Firebase Auth.

### com.example.studymate.ui.register

Handles the register fragment for the AuthActivity.

- **RegisterViewModel:** Manages the login component form state.
- **RegisterFragment:** Manages the register view and basic Firebase Logic for register.
- **RegisterFormState:** Manages errors in the RegisterFragment view form.

### com.example.studymate.ui.addFile

Manages the logic for adding new User Files with Text Recognition and Translation.

- **AddFileFragment:** Handles logic for registering a new file and contains the view layout.
- **PictureUtils:** Manages logic for rotating and resizing User File images.

### com.example.studymate.ui.locationSelector

Handles logic for picking a location when registering an account.

- **Location:** Location model for school locations.
- **LocationAdapter:** Adapter for the Location RecyclerView.
- **LocationItemFragment:** View item for the Location view adapter.

### com.example.studymate.ui.schedulerFragment

Manages logic for selecting schedule availability.

- **SchedulerFragment:** Fragment view handling logic for selecting availability.

### com.example.studymate.ui.mainFragments

Contains the three main folders for the main fragments.

#### com.example.studymate.ui.mainFragments.home

Manages logic for the home (Study Group) page.

- **Constants:** Contains constant values used in the application.
- **Department:** Model for storing Departments within the app.
- **DepartmentAdapter:** Adapter for the department RecyclerView.
- **HomeFragment:** Basic fragment when the app is opened, containing the RecyclerView and toolbar for departments.
- **PermissionUtils:** Contains logic for checking location permission in the app.
- **CourseRegisterFragment:** Manages the view and logic for registering a new course in Firebase.

#### com.example.studymate.ui.mainFragments.home.courseView

Handles logic for the different course view.

- **CourseAdapter:** Adapter for showing courses on screen, including course-related logic.
- **CoursesFragment:** Fragment storing the courses RecyclerView and a toolbar.

#### com.example.studymate.ui.mainFragments.home.studyGroupView

Manages the logic for the Study Group section of the app.

- **StudyGroupsFragment:** View and logic for displaying Study Groups, including logic for joining groups.
- **StudyGroupsAdapter:** Adapter for displaying StudyGroups in the RecyclerView.
- **RegisterStudyGroupFragment:** Responsible for handling adding new study groups via the form.

#### com.example.studymate.ui.mainFragments.home.groupView

Detail view for the Study Groups.

- **Message:** Basic model for storing a message from Firebase.
- **MessagesAdapter:** Adapter for rendering a message list to the RecyclerView.
- **GroupFragment:** Responsible for showing group information, name, creator, live chat, and holds chat logic.

#### com.example.studymate.ui.mainFragments.profile

Manages the logic for the profile page.

- **FileAdapter:** Contains logic for displaying User Files on the screen.
- **FileDetailFragment:** Fragment and view for the detail view per User File.
- **ProfileFragment:** Contains the view and logic for the profile page (logout, add a file, etc.).

#### com.example.studymate.ui.mainFragments.social

Manages the logic for the social (study Spot) page.

- **SocialFragment:** Contains logic for loading, fetching, and displaying Study Spots.
- **StudySpotAdapter:** Adapter for the SocialFragment RecyclerView (for study spots).
- **SubStudySpotAdapter:** Sub-adapter in each study spot for displaying sub-locations.
- **StudySpotRegisterFragment:** View for registering a new study spot, containing Firebase-related logic.

## Account Information

To log in with pre-generated data, use the following profile:

- **Username:** mzeolla
- **Password:** test123

Feel free to create a new account via the register page if needed.

## Local Development

If you're running the project locally, add the following key to the local.properties file:

```
MAPS_API_KEY=AIzaSyDdk85JplWqD5VH21aeGxiKcfnSXLS1TZg
```
