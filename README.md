<div align="center">

# 📅 Timetable Manager — Gestionnaire Intelligent d'Emplois du Temps

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![OOP](https://img.shields.io/badge/Design-Object--Oriented-blue?style=for-the-badge)](https://en.wikipedia.org/wiki/Object-oriented_programming)
[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

Application de planification académique développée en Java orienté objet pour les établissements universitaires et écoles d'ingénieurs : organisation des cours, détection automatique des conflits de créneaux, allocation des salles et planning des enseignants.

[Fonctionnalités](#-fonctionnalités) • [Modélisation](#-modélisation-métier) • [Installation](#-installation)

</div>

---

## 🌟 Fonctionnalités

- 👨‍🏫 **Gestion des Enseignants :** Fiche professeur, spécialités, volumes horaires hebdomadaires et indisponibilités.
- 🏫 **Parc de Salles & Équipements :** Types de salles (Amphithéâtres, Salles de TD, Laboratoires TP), capacités d'accueil et vidéo-projecteurs.
- 📚 **Groupes & Modules :** Gestion des filières, promotions, modules de cours, TD et séances de TP.
- ⚠️ **Moteur Anti-Collision de Créneaux :** Détection algorithmique en temps réel des conflits (double réservation d'un enseignant ou d'une salle sur le même horaire).
- 💾 **Persistance des Données :** Sauvegarde et chargement structuré des plannings (dossier `data/`).

---

## 🛠️ Stack & Principes de Conception

- **Langage :** Java (JDK 17+)
- **Architecture Logicielle :** Programmation Orientée Objet (POO), Design Patterns (MVC, Factory, Repository)
- **IDE Supportés :** Eclipse, IntelliJ IDEA, VS Code

---

## 📂 Structure du Répertoire

```bash
projet-gestion-emploi-du-temps-java/
├── src/
│   └── main/            # Code source Java (Classes métiers, Contrôleurs, Vues)
├── data/                # Fichiers de données et plannings sauvegardés
├── bin/                 # Fichiers compilés (.class)
├── .classpath           # Configuration Eclipse
└── .project             # Métadonnées de projet Eclipse
```

---

## 🚀 Installation & Exécution

### Option 1 : Avec un IDE (Eclipse / IntelliJ IDEA / VS Code)
1. Ouvrez votre IDE préféré.
2. Choisissez **Import Project** ➔ **Existing Projects into Workspace**.
3. Sélectionnez le dossier du dépôt cloné.
4. Lancez la classe principale contenant la méthode `main` (ex: `src/main/Main.java`).

### Option 2 : En ligne de commande (Terminal)
1. **Cloner le projet :**
   ```bash
   git clone https://github.com/yassir-el-manssouri/projet-gestion-emploi-du-temps-java.git
   cd projet-gestion-emploi-du-temps-java
   ```

2. **Compiler les fichiers Java :**
   ```bash
   javac -d bin src/main/*.java
   ```

3. **Exécuter l'application :**
   ```bash
   java -cp bin main.Main
   ```

---

## 👤 Auteur

- **Yassir EL MANSSOURI** - [@yassir-el-manssouri](https://github.com/yassir-el-manssouri) | [LinkedIn](https://www.linkedin.com/in/yassir-el-manssouri/)
- Étudiant / Ingénieur à l'École Marocaine des Sciences de l'Ingénieur (EMSI).

---


