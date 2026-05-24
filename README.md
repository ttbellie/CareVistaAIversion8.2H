# VistaCareAI - SIT305 Task 8.2HD

VistaCareAI is an Android health and wellness prototype developed for SIT305 Task 8.2HD. The app supports users with simple wellness tools including an AI health assistant, BMI calculator, daily health tracker, and period tracking feature.

The purpose of this prototype is to demonstrate how Generative AI can be integrated into a mobile application to improve the user experience and provide personalised wellness support.

## Features

- AI Health Assistant using Groq API and Llama 3.1
- BMI calculator with AI-generated wellness advice
- Daily health tracker for water intake, sleep, and mood
- AI-generated daily health insights
- Period tracking and prediction support
- Simple Android interface with clear navigation
- API error handling for invalid API keys, network issues, and usage limits

## LLM Integration

This app uses a hybrid LLM integration approach. The Android app provides the mobile user interface. User prompts are sent to the Groq API server. Groq runs the Llama 3.1 model remotely. The AI-generated response is returned and displayed in the app.

Model used: llama-3.1-8b-instant

API endpoint: https://api.groq.com/openai/v1/chat/completions

The LLM feature is used for general health and wellness Q&A, BMI advice generation, daily health insights, and period tracking insights.

## Privacy Note

This app uses remote AI inference through the Groq API. The user's typed prompt is sent to Groq servers for processing. The app does not intentionally store personal health data on remote servers. Health logs and period tracking data remain stored locally on the device.

Users are advised not to enter highly sensitive personal or medical information. The AI response is for general wellness information only and should not be treated as professional medical advice.

## Safety Handling

The AI assistant is designed to provide general wellness information only. The app does not diagnose medical conditions, prescribe medication, or provide emergency medical advice.

For serious medical concerns or emergencies, users should contact a qualified healthcare professional or emergency services.

If the API fails, the internet connection is unavailable, or the API limit is reached, the app displays a clear error message instead of generating fake AI responses.

## Technical Requirements

- Android Studio: Panda or newer stable version
- Language: Java
- Android Gradle Plugin: 8.7.3
- compileSdk: 35
- targetSdk: 35
- minSdk: 24
- Tested on API 35 emulator/device
- Tested on API 36 emulator/device

## Setup Instructions

1. Clone the repository: git clone https://github.com/ttbellie/CareVistaAIversion8.2H.git
2. Open the project folder in Android Studio.
3. Create a Groq API key from https://console.groq.com/keys
4. Open or create the local.properties file in the project root.
5. Add this line: GROQ_API_KEY=your_groq_api_key_here
6. Sync the project using File > Sync Project with Gradle Files.
7. Run the app using an Android emulator or physical Android device.

## Important API Key Note

The real API key must not be uploaded to GitHub. The project uses .gitignore to exclude local.properties from version control.

A sample local.properties.example file can be included with this line: GROQ_API_KEY=your_groq_api_key_here

## Android 16 Back Navigation Compatibility

The app uses standard Android Activity navigation and does not override custom back navigation behaviour. Because of this, the prototype remains compatible with Android 16 predictive back navigation requirements.

## Development Process

This prototype was developed through an iterative process. First, the base health and wellness application was built. Then, AI Assistant functionality was added. After that, Groq API with Llama 3.1 was integrated. The BMI, Health Tracker, and Period Tracker features were improved with AI-generated insights. API key security was improved using local.properties, and error handling was added for invalid API keys and network issues. Finally, compatibility was tested using API 35 and API 36 emulators.

## Known Limitations

- Internet connection is required for AI responses.
- The AI feature depends on Groq API availability.
- The app is not a medical diagnostic system.
- API usage limits may affect AI response availability.

## Future Work

- Add on-device LLM support for offline AI functionality.
- Improve menstrual cycle prediction accuracy.
- Add secure local database storage.
- Improve accessibility and responsive UI support.
- Add caching to reduce API usage.
- Add user authentication and cloud sync support.

## Screenshots

The submission folder includes screenshots of the main screen, AI Assistant feature, BMI feature, Health Tracker feature, and Period Tracker feature.

## Submission Evidence

This submission includes Android Studio project files, GitHub repository, README.md, screenshots, demo video, and presentation video.

## Author

SIT305 Task 8.2HD - VistaCareAI Prototype
