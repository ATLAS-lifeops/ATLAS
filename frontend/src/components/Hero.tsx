import { AtlasLogo } from './AtlasLogo';

export function Hero() {
  return (
    <header className="relative isolate min-h-[860px] overflow-hidden border-b border-atlas-border bg-atlas-bg px-5 py-8 sm:px-8 lg:min-h-[960px] lg:px-0">
      <div aria-hidden="true" className="hex-field hex-field-hero" />
      <div aria-hidden="true" className="hex-field hex-field-lower" />

      <nav
        aria-label="Main navigation"
        className="mx-auto flex max-w-[1120px] items-center justify-between border-b border-atlas-border pb-7"
      >
        <a
          aria-label="ATLAS home"
          className="group flex items-center gap-4 text-atlas-accent outline-none focus-visible:ring-2 focus-visible:ring-atlas-accent/70"
          href="#top"
        >
          <AtlasLogo className="h-9 w-9 transition-opacity group-hover:opacity-80" />
          <span className="text-lg font-medium tracking-[0.72em]">ATLAS</span>
        </a>

        <span className="hidden text-center text-[11px] font-medium uppercase tracking-[0.9em] text-atlas-muted lg:block">
          AI lifestyle operating system
        </span>

        <a className="btn-secondary h-12 px-6 text-xs" href="#telegram">
          Telegram
        </a>
      </nav>

      <div className="mx-auto flex max-w-[900px] flex-col items-center pt-20 text-center lg:pt-24">
        <AtlasLogo className="h-32 w-32 text-atlas-accent sm:h-40 sm:w-40" />
        <p className="mt-10 text-[11px] font-medium uppercase tracking-[0.85em] text-atlas-muted sm:text-xs">
          Multi-agent personal structure
        </p>
        <h1 className="mt-12 max-w-[920px] text-balance text-[clamp(3rem,6vw,4.875rem)] font-light leading-[1.12] tracking-normal text-atlas-text">
          Your personal operating system for life rhythm
        </h1>
        <p className="mt-9 max-w-[700px] text-balance text-lg leading-8 text-atlas-muted sm:text-xl">
          ATLAS helps coordinate training, planning, recovery, habits, nutrition,
          and weekly progress through a team of AI agents.
        </p>
        <div className="mt-12 flex w-full flex-col justify-center gap-4 sm:w-auto sm:flex-row">
          <a className="btn-primary" href="#telegram">
            Start with Telegram
          </a>
          <a className="btn-secondary" href="#system">
            View system
          </a>
        </div>
        <div className="mt-20 w-full rounded-[10px] border border-atlas-border bg-atlas-surface/80 px-6 py-8 shadow-atlas backdrop-blur sm:px-10">
          <p className="text-center text-[11px] font-medium uppercase leading-6 tracking-[0.45em] text-atlas-muted sm:text-xs">
            Core routing / Training load / Recovery / Weekly progress
          </p>
        </div>
      </div>
    </header>
  );
}
