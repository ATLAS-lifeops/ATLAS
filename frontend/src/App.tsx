import { AgentSystem } from './components/AgentSystem';
import { DashboardPreview } from './components/DashboardPreview';
import { Footer } from './components/Footer';
import { Hero } from './components/Hero';
import { HowItWorks } from './components/HowItWorks';

export default function App() {
  return (
    <div className="min-h-screen bg-atlas-bg text-atlas-text" id="top">
      <Hero />
      <main>
        <AgentSystem />
        <HowItWorks />
        <DashboardPreview />
      </main>
      <Footer />
    </div>
  );
}
