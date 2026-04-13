# 9003_FinalProject_GachaBox
# 🎰 GachaBox: The Digital Capsule Toy Experience

## 📖 Project Overview
Welcome to the official repository for **GachaBox**! This is our Final Project for **COMP9003**. 

GachaBox is a digital simulation of a physical capsule toy (Gashapon) machine. Our goal is to digitize the tactile joy of turning the dial and the anticipation of completing a collection, bringing the thrill of the gacha mechanism directly to mobile devices. 

### ✨ Core Features
* **Token System:** A virtual currency system managed locally, allowing users to "insert" tokens to play.
* **Dynamic Probability Engine:** A weighted random number generator (RNG) algorithm to manage drop rates (e.g., Common 70%, Rare 25%, Secret 5%).
* **Collection Gallery:** A visual inventory tracking unlocked items and displaying silhouettes for undiscovered hidden toys.

---

## 🛠️ Tech Stack
* **Environment:** Android Studio
* **Language:** Java
* **UI/Layout:** Android XML
* **Database:** SQLite / Room Persistence Library
* **Version Control:** Git & GitHub

---

## 👥 Team Roles & Responsibilities

To ensure equal contribution and efficient development, our group of 3 is divided into the following core modules:

### 🎨 Member 1: UI/UX & Animations (Frontend)
* **Name:** [rui]
* **Responsibilities:**
    * Design and build all XML layout files (Main Interface, Gacha Pull Screen, Collection Gallery).
    * Implement Android UI components (RecyclerView for the gallery, ConstraintLayout).
    * Develop visual feedback and simple animations (e.g., the capsule dropping effect).

### ⚙️ Member 2: Core Logic & RNG Algorithm (Backend/Engine)
* **Name:** [feng]
* **Responsibilities:**
    * Design and implement the Weighted Random Generation algorithm in Java to handle realistic drop rates.
    * Manage the application's state machine (e.g., checking if the user has enough tokens before a pull).
    * Develop the core Java classes for the Gacha mechanics.

### 🗄️ Member 3: Database & Data Persistence (Storage)
* **Name:** [Zhou]
* **Responsibilities:**
    * Set up the local SQLite database (or Room).
    * Design database schemas for the `User` (Token balance) and `Inventory` (Unlocked gacha items).
    * Write CRUD (Create, Read, Update, Delete) operations to ensure the UI and backend logic properly sync with local storage.

---

## 📅 Development Roadmap & Milestones

* **Phase 1: Planning & Setup (Weeks 4-5)**
    * Set up GitHub repository and branch protection.
    * Initialize the Android Studio project and test AVD (Android Virtual Device) compatibility.
    * Finalize UI wireframes and database schema.
* **Phase 2: Core Development (Weeks 6-8)**
    * Build static XML layouts.
    * Implement the Java RNG probability engine.
    * Establish SQLite database connections.
* **Phase 3: Integration & UI Sync (Weeks 9-10)**
    * Connect Frontend buttons to Backend logic.
    * Ensure database updates correctly when a gacha is pulled.
* **Phase 4: Polish & Delivery (Weeks 11-12)**
    * Implement animations and refine UI.
    * Bug fixing and AVD stability testing.
    * Prepare Padlet showcase and Final Reflection Document.

---

## 🚀 Team Sync Rules

* **Always PULL** before you start coding to get the latest updates.
* **COMMIT & PUSH** at least once a day or whenever you finish a small task.
* **DO NOT** send code via WeChat/ZIP files. Only use GitHub.
* **Write clear commit messages** (e.g., "Updated UI for gallery" instead of "1111").