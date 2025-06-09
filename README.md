# TaskSync PMS

Welcome to **TaskSync PMS**, a project management system I built for my high school computer science class!  
This app helps teams organize projects, manage tasks, and collaborate through comments. It’s built with a **Java + Spring Boot** backend and a **React + TypeScript** frontend. I put a lot of effort into this and I’m really excited to share it!

![Screenshot From 2025-05-07 11-23-24](https://github.com/user-attachments/assets/11e3be25-a8c2-4b78-9434-24f143a7da1c)





![Screenshot From 2025-05-07 11-29-39](https://github.com/user-attachments/assets/7c8cebcd-4670-44de-861d-8d290e7d6e88)




![Screenshot From 2025-05-07 11-36-55](https://github.com/user-attachments/assets/ed1abdc3-319a-4726-9d00-3b00e80fcfd5)

![Screenshot From 2025-05-07 11-37-23](https://github.com/user-attachments/assets/47b269e6-19bd-4fe0-807d-48917468a385)

---

## 🌟 Features

- **User Registration & Login**: Sign up as a regular user or a team lead. Team leads have extra privileges like creating teams and assigning tasks.
- **Team Management**: Team leads can create teams and add registered users as members.
- **Project & Task Tracking**: Create projects within teams and add tasks with statuses like "To Do", "In Progress", or "Done".
- **Comments**: Users can comment on tasks to discuss progress or share updates.
- **Role-Based Access**: Regular users can view teams and tasks, while team leads can create, edit, or delete them.
- **Responsive UI**: The interface is clean and works decently on desktop and mobile (still polishing the mobile part!).

---

## 🛠️ Tech Stack

### Backend

- **Java 17**
- **Spring Boot**
- **Spring Security** (JWT-based authentication)
- **PostgreSQL**
- **Maven**

### Frontend

- **React**
- **TypeScript**
- **Tailwind CSS**
- **React Router**
- **Vite**

---

## 🚀 How to Run It

Tested on Windows 11, should also work on Mac or Linux.

### Prerequisites

- Java 17 (OpenJDK recommended)
- Node.js 18+
- PostgreSQL 15
- Maven
- npm (comes with Node.js)

### Backend Setup



git clone https://github.com/your-username/tasksync-pms.git
cd tasksync-pms


### Set up PostgreSQL:



- Create a database named tasksync.

- Update your credentials in src/main/resources/application.properties:

properties

spring.datasource.url=jdbc:postgresql://localhost:5432/tasksync
spring.datasource.username=your-username
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update

- Build and run the backend:



mvn clean install
mvn spring-boot:run
The backend will be available at http://localhost:8080.







### Frontend Setup
In a new terminal:



cd frontend
npm install
npm run dev
The frontend will be available at http://localhost:8081.

###  💻 Using the App
- Go to http://localhost:8081/register to sign up.

- Choose TEAM_LEAD to create teams or USER to join them.

- After login, team leads can create teams and add members.

- Click a team → View projects → View tasks.

- Team leads can update tasks; all users can add comments.

### 🗄️ Database Schema
The app uses the following tables:

users: id, first_name, last_name, email, password, role

teams: team names and IDs

team_members: many-to-many relation between users and teams

projects: linked to teams

tasks: title, status, assignee

comments: task comments

tokens: JWT access and refresh tokens




### ⚠️ Known Issues
Team list might not load right after login — a refresh usually fixes it.

Mobile responsiveness needs work.

Error messages could be more user-friendly.






### 🔮 Future Ideas
Search users when adding to teams.

Let team leads remove members.

Notifications for task updates.

UI improvements (colors, animations, mobile polish).








### 💡 Why I Built This
I wanted to challenge myself to create a full-stack app with real-world features like authentication and permissions.
Learning JWT and React context was tricky, but rewarding! I thought a project management tool would be perfect for group work—and my teacher liked the idea too.



###  🤝 Contributing
This is a school project, so I’m not accepting contributions yet.
But feel free to open an issue if you spot bugs or have suggestions!




### 📜 License
There’s no formal license yet—this is a personal school project.
Please don’t copy it for your own homework. 😅










Thanks for checking out TaskSync PMS!

I hope you like it as much as I enjoyed building it!

If you have feedback, drop it in the issues section. 😊

<p align="center"> <a href="https://github.com/your-username/tasksync-pms/issues">Report a Bug</a> | <a href="https://github.com/your-username/tasksync-pms">View on GitHub</a> </p> ```
