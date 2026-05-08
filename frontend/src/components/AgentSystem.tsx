import { agents } from '../data/agents';

function MiniHex() {
  return (
    <svg
      aria-hidden="true"
      className="h-7 w-7 text-atlas-accent/75"
      fill="none"
      viewBox="0 0 28 28"
    >
      <polygon
        points="14 1.5 24.8 7.75 24.8 20.25 14 26.5 3.2 20.25 3.2 7.75"
        stroke="currentColor"
        strokeWidth="1.2"
      />
    </svg>
  );
}

export function AgentSystem() {
  return (
    <section
      className="section-shell border-b border-atlas-border"
      id="system"
    >
      <div className="section-heading">
        <p className="section-kicker">01 / Agent system</p>
        <h2>A quiet team for daily structure</h2>
        <p>
          Each agent handles a narrow operating domain, while ATLAS Core routes
          context and keeps the system coherent.
        </p>
      </div>

      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {agents.map((agent) => (
          <article className="atlas-card min-h-[174px]" key={agent.name}>
            <MiniHex />
            <h3 className="mt-7 text-lg font-medium text-atlas-text">
              {agent.name}
            </h3>
            <p className="mt-3 max-w-[14rem] text-sm leading-6 text-atlas-muted">
              {agent.summary}
            </p>
            <div className="mt-7 h-px w-full bg-atlas-border" />
          </article>
        ))}
      </div>

      <p className="mt-16 text-center text-xs font-medium uppercase tracking-[0.55em] text-[#5D615E]">
        Telegram as the interface. Agents as the operating layer.
      </p>
    </section>
  );
}
