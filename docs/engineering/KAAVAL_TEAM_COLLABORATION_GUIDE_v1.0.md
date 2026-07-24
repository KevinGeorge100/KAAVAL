# KAAVAL Team Collaboration & Project Management Standard Guide v1.0

**Project Name:** KAAVAL — Accessibility-First Emergency Response Ecosystem  
**Target Program:** IEEE Sensors Council Industry Mentoring Program  
**Team Composition:** 4 Team Members + Project Guide / Mentor  
**Tools Stack:** ClickUp (Project Management), Slack (Communication), GitHub (Version Control & CI/CD)  

---

## 1. Team Organization & Role Responsibilities

| Name | Role | Core Domain & Deliverables |
| :--- | :--- | :--- |
| **Project Guide / Mentor** | **Industry & Academic Mentor** | Milestone sign-offs, architectural oversight, sprint evaluations. |
| **Kevin George** | **Software Lead & Architect** | Android App, Voice/Haptic Engine, SOS State Machine, Firebase Cloud, OpenAI API integration. |
| **Navami** | **EEE Hardware Lead** | Microcontroller selection (ESP32-C3 / nRF52840), schematic capture, debouncing logic. |
| **Adwaid** | **EEE Power Systems Lead** | TP4056 USB Type-C charging circuit, battery protection, LDO regulator, power optimization. |
| **Jewel** | **EEE Enclosure & CAD Lead** | 3D CAD Wristband/Pendant model, tactile button ergonomics, water-resistant casing assembly. |

---

## 2. ClickUp Industry Project Management Setup

### 2.1 ClickUp Hierarchy Structure
```text
Workspace: KAAVAL Ecosystem
  └── Space 1: 📱 Software Engineering
        ├── Folder: Android Client App (Kotlin / Compose)
        ├── Folder: Cloud & Backend (Firebase & OpenAI API)
        └── Folder: Tracking Web Portal
  └── Space 2: ⚡ Hardware Engineering
        ├── Folder: BLE Wearable PCB & Circuit Schematic
        ├── Folder: Power & Battery Management
        └── Folder: 3D CAD Enclosure & Mechanical Design
  └── Space 3: 🎓 Project Management & Guide Reviews
        ├── List: Sprint Backlog & User Stories
        ├── List: IEEE Mentoring Milestones
        └── List: Weekly Progress Reports
```

### 2.2 Standard ClickUp Task Statuses
1. 🔴 **Backlog**: Refined tasks waiting for sprint allocation.
2. 🟡 **In Progress**: Active development task.
3. 🔵 **In Review**:
   - Software: Open GitHub Pull Request (PR).
   - Hardware: Schematic / CAD file awaiting peer/mentor review.
4. 🟣 **Testing / QA**: Device / APK testing phase.
5. 🟢 **Done**: Tested, merged to `main`, and signed off.

---

## 3. Slack Workspace & Communication Rules

### 3.1 Official Channel Blueprint
- **`#kaaval-announcements`**: Official announcements, mentor feedback, and milestone deadlines.
- **`#kaaval-dev-android`**: Android Kotlin development, Jetpack Compose, accessibility bugs.
- **`#kaaval-hardware-eee`**: Circuit schematics, component sourcing, BLE testing, 3D printing.
- **`#kaaval-ai-openai`**: OpenAI API prompt engineering, situation summarizer testing.
- **`#kaaval-github-bot`**: Live feed of commits, PRs, and build statuses.

### 3.2 Daily Async Standup (Every Weekday by 10:00 AM)
Every team member posts 3 short bullet points in `#kaaval-announcements`:
```text
1. 🟢 What I completed yesterday:
2. 🟡 What I am working on today:
3. 🔴 Blockers (if any):
```

---

## 4. GitHub Industry Development Workflow

### 4.1 Git Branching Architecture
- **`main`**: Protected release branch. Directly deployed & stable.
- **`develop`**: Integration branch for upcoming sprint releases.
- **Feature Branches**:
  - `feature/android-sos-countdown` (Kevin)
  - `hardware/eee-esp32-schematic` (Navami)
  - `hardware/eee-power-circuit` (Adwaid)
  - `hardware/eee-cad-wristband` (Jewel)

### 4.2 Commit Message Convention (Conventional Commits)
All commit messages must follow the industry format:
- `feat(android): add voice feedback engine for English & Malayalam`
- `feat(ai): integrate OpenAI API emergency context summarizer`
- `hardware(eee): add TP4056 charging schematic in KiCAD`
- `docs(guide): update sprint review progress report`

### 4.3 Pull Request (PR) Standard
- Every PR must link to its corresponding **ClickUp Task ID**.
- Code changes require at least **1 peer review** before merging into `main`.

---

## 5. Weekly Project Guide Review Process
1. **Friday Progress Summary**: Auto-compiled ClickUp report sent to Project Guide.
2. **Bi-Weekly Live Demo**: Live demonstration of Android APK build + EEE hardware prototype progress.
