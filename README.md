# The F-Kult application!

The F-Kult Platform is a centralized web application developed to address the fragmentation of F-Kult event-related activities by consolidating them into a single, unified system. The platform serves as a central event hub, enabling members to stay informed about upcoming, past, and potential future events, along with their associated content.

The system allows members to upload and browse movie themes and sound samples, which are stored in an SQLite database. This functionality supports collaborative content sharing and facilitates structured event planning and organization.

Administrators are granted extended access rights, allowing them to create and manage events, moderate uploaded content, play sound samples, and maintain an overview of member contributions. Furthermore, administrators can initiate voting processes to determine which movie themes will be selected for upcoming semesters.

The frontend of the application is developed using JavaScript with React and Tailwind CSS, providing a responsive and user-friendly interface. The backend is implemented in Java using the Maven framework and integrates with an SQLite database. The database is updated weekly using IMDb’s non-commercial datasets through API integration, supplemented by web scraping and rich preview techniques to collect movie posters and related metadata.

## Features
- **User Management**: Secure login system with role-based access for members and administrators.
- **Event Management**: Tools for creating, editing, and managing F-Kult events and schedules.
- **Theme Submission**: Members can upload and submit movie themes for upcoming events.
- **Sound Sample Management**: Uploading, browsing, and playback of sound samples.
- **Content Browsing**: Browse and explore themes and sound samples shared by other members.
- **Voting System**: Integrated voting system for selecting movie themes for future semesters.
- **Admin Dashboard**: Centralized interface for administrators to manage users, content, and events.
- **Database Integration**: SQLite-based data storage for reliable and lightweight data management.
- **IMDb Integration**: Automated weekly updates using IMDb datasets and API integration.
- **Media Preview System**: Automatic retrieval of movie posters using web scraping and rich preview techniques.
- **Next Theme Poster Preview**: Preview page displaying posters and metadata for the upcoming movie theme, for use of advertisement.
- **Responsive Interface**: Modern, responsive UI built with React and Tailwind CSS.
- **Security & Access Control**: Protected routes and permission-based system access.

### Routes
Below is a list of all available routes in the F-Kult web application.
#### Public Routes (No Authentication Required)
- Home page (root): `/` 
- Login page: `/login`
- Showcase upcoming event poster: `/showcase`

#### Protected Routes (Login Required)
- User homepage: `/{username}` 
- Theme browser: `/themes/{username}`
- Submit sound samples: `/submit/{username}`
- Sound sample browser: `/sound-samples/{username}`
- FAQ page: `/faq/{username}`

#### Admin Routes (Admin Access Required)
- Admin dashboard: `/admin/{username}` 
- Wheel of Fortune: `/admin/wheel/{username}`
- View sound samples: `/admin/sound-sample/{username}`
- Theme voting page: `/admin/voting/{username}`

## Technologies Used
- **Frontend:** React, Tailwind (with Vite)
- **Backend:** Java (Spring Boot)
- **Database:** SQLite
- **Build/Dependency Management:** Maven (via Spring Boot)

## Project Structure
```plaintext
P3/
├── backend/fkult/                          # Backend code
│   ├── database/                           # Local database 
│   ├── soundSampleUploads/                 # Storage for sound sample files
│   └── src/
│       ├── it/
│       │   ├── java/com/p3/fkult/it/       # Integration tests
│       │   └── resources/                  # Database for testing
│       ├── main/                           # Contains main functionality
│       │   ├── java/com/p3/fkult/          # Root package for backend source code
│       │   │    ├── business/services/     # Core logic and rules for processing requests
│       │   │    ├── config/                # Spring configuration (CORS, schedulers, startup initialization tasks)
│       │   │    ├── persistence/           # Handles database communication
│       │   │    │   ├── entities/          # Persistence models representing database tables
│       │   │    │   └── repository/        # Communication to Database
│       │   │    └── presentation/          # API layer/REST interface
│       │   │        ├── controllers/       # REST controllers (endpoints)
│       │   │        └── DTOs/              # Data Transfer Objects for requests/responses
│       │   └── resources/                  # Database layout/setup
│       └── test/                           # Unit tests
└── frontend/                               # Frontend code
    ├── public/                             # Images
    └── src/
        ├── assets/                         # Images, sounds and such
        ├── components/                     # Where Specific UI components are stored
        ├── locales/                        # Defined const for translation (en-da)
        ├── pages/                          # Where entire Page components are stored
        ├── services/                       # Bridge to the Backend
        └── Test/                           # Tests
```

## Getting Started
### Prerequisites
- **Java JDK**: required to run the Spring Boot backend.
- **Maven**: The Maven Wrapper (mvnw) handles installation automatically.
- **Node.js & npm**: Required for the frontend (Vite + React).
- **SQLite**: For database.

### Setup the project
1.	Clone the repository:
```bash
git clone https://github.com/f-klubben/F-Kult.git
```

2. Navigate to the project directory:
```bash
cd F-Kult
```

3. Install dependencies and initialize the database

The first startup may take 5–10 minutes, since the backend downloads and imports over 1 million movie data.
The process is complete when the following message appears:
`[IMDb bootstrap] Import complete. rows_processed={large number}`

```bash
# PowerShell (Windows):
cd frontend
npm install
cd ../backend/fkult
.\mvnw.cmd spring-boot:run 

# Mac/Linux:
cd frontend
npm install
cd ../backend/fkult
./mvnw spring-boot:run
```

### Manual Database Setup (Optional)
If you want to initialize or refresh the movie database manually without starting the backend server, you can run the setup command:

```bash
# PowerShell (Windows)
cd backend/fkult
.\SetupDatabase.bat
```

### Running the App for the First Time
Open **two terminals** from the root `F-Kult`. In the first terminal, start the backend:
```bash
# PowerShell (Windows):
cd backend/fkult; .\mvnw.cmd spring-boot:run

# Mac/Linux:
cd backend/fkult && ./mvnw spring-boot:run
```

In the second terminal, run the frontend:
```bash
# PowerShell (Windows):
cd frontend; npx vite

# Mac/Linux:
cd frontend && npx vite
```

Then open: [http://localhost:5173/](http://localhost:5173/)

### Running tests
Assumes you are in the project root `F-Kult`

#### Backend testing
```bash
# PowerShell (Windows):
cd backend/fkult; .\mvnw.cmd test;

# Mac/Linux:
cd backend/fkult && ./mvnw test
```

#### Frontend testing
Checks if the backend is running before executing frontend tests. If the backend is unavailable, three frontend tests will fail.
```bash
# PowerShell (Windows):
cd frontend; npm run test

# Mac/Linux:
cd frontend && npm run test
```

### Run both tests at the same time
```bash
# PowerShell (Windows):
cd backend/fkult; .\mvnw.cmd test; cd ../../frontend; npm run test

# Mac/Linux:
cd backend/fkult && ./mvnw test && cd ../../frontend && npm run test
```

# Contributing
Contributions are welcome! Follow these steps:
1. Make sure you are on `staging` and it is up to date.
```bash
git switch staging
git pull
```

2. Create a branch for your feature or bugfix:
```bash
git switch -c feat/feature-name
git switch -c bugfix/bug-name
```

3. Commit your changes:
```bash
git commit
# Write commit message in opened editor, save and exit editor
```

4. Push local branch to remote:
```bash
git push origin feature-name
```
5. Open a pull request to the `staging` branch, test it, and then create a new pull request for main.

# The original creators of the F-Kult application
- Alex Enggaard Jensen: [Alenje2004](https://github.com/Alenje2004)
- Frederik Christensen: [Fzz1n](https://github.com/Fzz1n)
- Jonas Bjerregaard-Pedersen: [JBPio](https://github.com/JBPio)
- Martin Riber Thomsen: [Kabuum](https://github.com/Kabuum)
- Mikkel Ganderup: [Mikkelgan](https://github.com/Mikkelgan)
- Nikolai Medom Jensen: [NikolaiMJ-Software](https://github.com/NikolaiMJ-Software)
- Sebastian Łukasz Brechun: [Sebastianplaygames](https://github.com/Sebastianplaygames)