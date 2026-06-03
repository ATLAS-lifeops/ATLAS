# ATLAS Life Flows

ATLAS is a multi-agent life tracker in Telegram. It helps the user understand state, keep focus, track habits, plan realistically, reflect in the evening and see progress over time.

Movement, training, nutrition and recovery are life loops inside the product. They are not the whole product.

## Main Loop

```text
onboarding
daily check-in
day plan
habits
evening reflection
weekly report
```

## Commands

```text
/start      onboarding or welcome-back message
/checkin    daily state check-in
/day        realistic day plan
/habits     habit tracking
/evening    evening reflection
/review     evening reflection alias
/report     weekly report
/cancel     cancel current flow
/help       command guide
/emergency  minimal plan when the day falls apart
```

## Persistence

ATLAS persists Telegram users, messages, life profiles, active conversation states, check-ins, habit checks and evening reflections in PostgreSQL. Active flows are stored so a user can continue after an application restart.

## Safety

ATLAS is not a doctor, dietitian, therapist or medical specialist. It does not diagnose, prescribe treatment or recommend ignoring serious symptoms. When a message mentions pain, injury, chest pain, breathing issues, dizziness, fainting, blood pressure, heart symptoms or severe overload, ATLAS responds calmly and suggests contacting a qualified professional for serious symptoms.
