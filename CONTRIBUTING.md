# Contributing to Topspin 🏓

First off, thank you for considering contributing to Topspin! We welcome any help to make this the best table tennis training planner. Whether it's reporting a bug, discussing features, or writing code, your involvement is valuable.

Please take a moment to review this document to understand how you can contribute.

## Code of Conduct

This project and everyone participating in it is governed by the [Topspin Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to b.sommerfeld2003@gmail.com.

## Ways to Contribute

There are many ways to contribute:

* **Reporting Bugs:** If you find a bug, please check if it's already reported in the [Issues](https://github.com/Metaphoriker/topspin/issues). If not, please submit a new bug report using the "Bug Report" template.
* **Suggesting Enhancements:** Have an idea for a new feature or an improvement? Check the [Issues](https://github.com/Metaphoriker/topspin/issues) first, and if it's a new idea, submit it using the "Feature Request" template.
* **Code Contributions:** If you want to fix a bug or implement a feature, please follow the process described below. Look for issues tagged `good first issue` or `help wanted` if you're looking for a place to start.
* **Documentation:** Improving the README, adding documentation, or writing tutorials is always helpful.
* **Feedback:** Providing feedback on existing issues or pull requests is valuable.
* **Spread the Word:** Tell your friends, club mates, or coaches about Topspin!

## Getting Started (Development Setup)

Ready to contribute code? Here’s how to set up your development environment:

1.  **Prerequisites:**
    * [Git](https://git-scm.com/)
    * Java Development Kit (JDK) - Version 22 or later.
    * [Apache Maven](https://maven.apache.org/) (or use the included Maven Wrapper `./mvnw`)
    * An IDE like IntelliJ IDEA, Eclipse, or VS Code with Java support is recommended.

2.  **Fork & Clone:**
    * Fork the repository on GitHub using the "Fork" button.
    * Clone your fork locally:
        ```bash
        git clone https://github.com/Metaphoriker/topspin.git
        cd topspin
        ```

3.  **Build the Project:**
    * Ensure the project builds correctly using the Maven Wrapper:
        ```bash
        # On Linux/macOS
        ./mvnw clean install

        # On Windows
        .\mvnw.cmd clean install
        ```
    * Import the project into your IDE (it should recognize it as a Maven project).

## Submitting Changes (Pull Request Process)

1.  **Create a Branch:** Create a new branch for your changes, based on the `main` branch:
    ```bash
    git checkout main
    git pull origin main # Ensure you have the latest changes from upstream
    git checkout -b feature/your-descriptive-feature-name
    # or for bug fixes:
    # git checkout -b fix/short-bug-description
    ```

2.  **Make Changes:** Write your code, following the existing code style. Add comments for complex logic.

3.  **Add Tests:** If you are adding new features or fixing bugs, please try to add corresponding unit tests to verify your changes.

4.  **Ensure Build & Tests Pass:** Before submitting, run the build and tests again:
    ```bash
    ./mvnw clean verify
    ```

5.  **Commit Changes:** Write clear and concise commit messages. Consider following the [Conventional Commits](https://www.conventionalcommits.org/) specification.

6.  **Push Changes:** Push your branch to your fork:
    ```bash
    git push origin feature/your-descriptive-feature-name
    ```

7.  **Open a Pull Request (PR):**
    * Go to the original Topspin repository on GitHub.
    * Click on "New Pull Request".
    * Choose your fork and branch to compare against the `main` branch of the original repository.
    * Fill out the Pull Request template, describing your changes clearly and linking to any related issues (e.g., `Closes #123`).
    * Submit the PR!

8.  **Review:** Be prepared to discuss your changes and respond to feedback from maintainers or other contributors.

## Issue Tracking

* Use GitHub Issues to report bugs and request features (please use the provided templates!).
* Check existing issues before creating a new one to avoid duplicates.
* Feel free to comment on existing issues to provide more context or indicate your interest in working on them.

## Code Style

Please try to follow the existing code style found throughout the project. Consistency makes the codebase easier to read and maintain.

## Questions?

If you have questions about contributing, feel free to:

* Ask on our [Discord Server](https://discord.gg/pMBdw4q5bV).
* Open an issue if it's specific to a bug or feature proposal.

Thank you for helping make Topspin better!
