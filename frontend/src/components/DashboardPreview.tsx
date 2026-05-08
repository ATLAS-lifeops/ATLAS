function ProgressBar({ value }: { value: number }) {
  return (
    <div className="mt-5 h-1 rounded-full bg-atlas-border">
      <div
        className="h-full rounded-full bg-atlas-accent"
        style={{ width: `${value}%` }}
      />
    </div>
  );
}

function PreviewPanel({
  label,
  value,
  note,
  children,
  className = '',
}: {
  label: string;
  value: string;
  note: string;
  children?: ReactNode;
  className?: string;
}) {
  return (
    <article className={`dashboard-panel ${className}`}>
      <p className="text-[11px] font-medium uppercase tracking-[0.55em] text-atlas-muted">
        {label}
      </p>
      <p className="mt-4 text-2xl font-normal leading-9 text-atlas-text sm:text-[26px]">
        {value}
      </p>
      <p className="mt-4 text-sm leading-6 text-atlas-muted">{note}</p>
      {children}
    </article>
  );
}

export function DashboardPreview() {
  return (
    <section className="section-shell border-b border-atlas-border">
      <div className="section-heading">
        <p className="section-kicker">03 / System preview</p>
        <h2>A calm command surface for the week</h2>
        <p>
          The assistant keeps state visible without turning life into a noisy
          dashboard.
        </p>
      </div>

      <div className="mt-20 rounded-[14px] border border-atlas-border bg-[#07090B] p-0 shadow-atlas">
        <div className="flex flex-col gap-3 border-b border-atlas-border bg-atlas-surface px-6 py-5 sm:flex-row sm:items-center sm:justify-between">
          <p className="text-xs font-medium uppercase tracking-[0.7em] text-atlas-muted">
            ATLAS / Today
          </p>
          <p className="text-xs font-medium uppercase tracking-[0.5em] text-[#5D615E]">
            Week 18 / Controlled load
          </p>
        </div>

        <div className="grid gap-6 p-6 lg:grid-cols-12">
          <PreviewPanel
            className="lg:col-span-4"
            label="Today’s readiness"
            note="Proceed with planned strength block."
            value="82 / 100"
          >
            <ProgressBar value={82} />
          </PreviewPanel>

          <PreviewPanel
            className="lg:col-span-5"
            label="Training plan"
            note="Estimated duration 72 min. Keep final set submaximal."
            value="Lower strength + Zone 2"
          >
            <div className="mt-5 h-px bg-atlas-border" />
          </PreviewPanel>

          <PreviewPanel
            className="lg:col-span-3"
            label="Sleep status"
            note="Latency improved. Fatigue trend stable."
            value="7h 42m"
          >
            <div className="mt-5 flex items-center gap-2">
              {[46, 66, 96].map((width, index) => (
                <span
                  aria-hidden="true"
                  className="h-1 rounded-full bg-atlas-accent"
                  key={width}
                  style={{ opacity: 0.45 + index * 0.2, width }}
                />
              ))}
            </div>
          </PreviewPanel>

          <PreviewPanel
            className="lg:col-span-4"
            label="Habit streaks"
            note="Morning walk, mobility, journal, water, shutdown."
            value="5 active streaks"
          >
            <div className="mt-5 flex gap-3">
              {Array.from({ length: 7 }, (_, index) => (
                <span
                  aria-hidden="true"
                  className={`mini-filled-hex ${
                    index < 5 ? 'opacity-80' : 'opacity-20'
                  }`}
                  key={index}
                />
              ))}
            </div>
          </PreviewPanel>

          <PreviewPanel
            className="lg:col-span-5"
            label="Nutrition focus"
            note="Anchor meals around training. Keep evening light."
            value="Protein + timing"
          >
            <ProgressBar value={66} />
          </PreviewPanel>

          <PreviewPanel
            className="lg:col-span-3"
            label="Weekly progress"
            note="Four of five target behaviors above baseline."
            value="+14% consistency"
          >
            <div className="mt-5 grid grid-cols-4 items-end gap-2">
              {[44, 62, 32, 78].map((height, index) => (
                <span
                  aria-hidden="true"
                  className="w-full rounded-full bg-atlas-accent/70"
                  key={index}
                  style={{ height }}
                />
              ))}
            </div>
          </PreviewPanel>
        </div>
      </div>
    </section>
  );
}
import type { ReactNode } from 'react';
