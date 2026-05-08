export type Agent = {
  name: string;
  summary: string;
};

export const agents: Agent[] = [
  {
    name: 'ATLAS Core',
    summary: 'Coordination and request routing',
  },
  {
    name: 'ATLAS Coach',
    summary: 'Training and load',
  },
  {
    name: 'ATLAS Planner',
    summary: 'Daily and weekly schedule',
  },
  {
    name: 'ATLAS Recovery',
    summary: 'Sleep, fatigue, recovery',
  },
  {
    name: 'ATLAS Habits',
    summary: 'Discipline and rhythm',
  },
  {
    name: 'ATLAS Fuel',
    summary: 'Nutrition for your goal',
  },
  {
    name: 'ATLAS Report',
    summary: 'Weekly analytics',
  },
];
