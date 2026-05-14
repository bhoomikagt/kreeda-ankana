<div align="center">

# 🏏 Kreeda Ankana
### Smart Sports Ground Booking & Matchmaking Platform

<p align="center">
  <img src="[https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)" alt="Android" />
  <img src="[https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)" alt="Kotlin" />
  <img src="[https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge](https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge)" alt="MVVM" />
  <img src="[https://img.shields.io/badge/Firebase-Backend-FFCA28?style=for-the-badge&logo=firebase&logoColor=black](https://img.shields.io/badge/Firebase-Backend-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)" alt="Firebase" />
  <img src="[https://img.shields.io/badge/Cloud-Functions-orange?style=for-the-badge](https://img.shields.io/badge/Cloud-Functions-orange?style=for-the-badge)" alt="Cloud Functions" />
  <img src="[https://img.shields.io/badge/Status-Active-success?style=for-the-badge](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)" alt="Status" />
</p>

<p align="center">
  <strong>Connecting Players, Teams & Villages Through Sports Technology 🚀</strong>
</p>

<p align="center">
  Kreeda Ankana is a smart Android application designed to solve sports coordination challenges in villages and local communities by enabling slot booking, team creation, opponent finding, match scheduling, and real-time communication.
</p>

---

</div>

## 📌 Problem Statement

In many villages and local communities, sports grounds such as **Cricket**, **Volleyball**, and other play areas are often occupied by the same groups throughout the day. 

This creates several problems:
* ❌ No proper **slot booking system** for grounds  
* ❌ Difficulty in **finding opponent teams** for friendly matches  
* ❌ No centralized way to **organize matches** between villages  
* ❌ Poor coordination between teams and players  
* ❌ Manual scheduling causes **conflicts and confusion**

### 💡 The Solution

**Kreeda Ankana** solves this problem by creating a **digital sports ecosystem** where teams can:
* ✅ Book sports grounds  
* ✅ Find opponents nearby  
* ✅ Schedule matches efficiently  
* ✅ Send match invitations  
* ✅ Join challenge boards  
* ✅ Communicate in real time

---

## 🎯 Vision

The vision of **Kreeda Ankana** is to empower villages and local communities through sports by providing a centralized digital platform for:

* 🏏 **Sports Ground Management**
* 🤝 **Team Collaboration**
* ⚔️ **Friendly Match Challenges**
* 📅 **Match Scheduling**
* 💬 **Seamless Communication**

The platform aims to increase sports participation, reduce scheduling conflicts, and encourage healthy competition among communities.

---

## ✨ Key Features

### 🔐 Authentication System
Secure and flexible user authentication powered by Firebase.
* **Supported Methods:** Google Authentication, Phone Number, and Email.
* **Benefits:** Secure login experience, fast onboarding, and multiple authentication options.

### 🏟️ Ground Slot Booking
Book sports grounds efficiently without conflicts.
* **Features:** View available grounds, select preferred slots, avoid overlapping bookings.
* **Benefits:** Prevents the same teams from monopolizing grounds and enables fair access.

### 👥 Team Creation & Management
Players can create and manage teams seamlessly.
* **Features:** Create sports teams, add/manage details, and organize players.
* **Benefits:** Better team coordination and simplified management.

### 🤝 Friendly Match Invite System
Host teams can invite opponent teams for friendly matches.
* **Features:** Send, accept, or reject match invitations.
* **Benefits:** Makes organizing village-level sports matches significantly easier.

### ⚔️ Challenge Board
Teams can publicly post challenges.
* **Features:** Open challenge posting and public challenge acceptance.
* **Benefits:** Improves sports engagement and team interactions.

### 🔍 Opponent Finding
Find active teams nearby.
* **Features:** Discover teams and connect for matches.
* **Benefits:** Small teams can easily organize games instead of struggling to find players.

### 📅 Match Scheduling
Schedule sports matches effectively.
* **Features:** Match timing management, organized scheduling, calendar-based planning.
* **Benefits:** Avoids scheduling conflicts and improves organization.

### 💬 Real-Time Chat
Instant communication between teams and players.
* **Features:** Real-time messaging, match discussions, team coordination.
* **Benefits:** Reduces communication gaps and simplifies planning.

---

## 🏗️ System Architecture

Kreeda Ankana follows the **MVVM (Model-View-ViewModel)** architecture pattern to ensure maintainability and scalability.

### Architecture Flow
```text
UI Layer (XML)
       ↓
ViewModel Layer
       ↓
Repository Layer
       ↓
Firebase Services
       ↓
Firestore Database + Cloud Functions
