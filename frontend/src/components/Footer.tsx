import { AtlasLogo } from './AtlasLogo';

export function Footer() {
  return (
    <footer className="px-5 py-20 sm:px-8 lg:px-0" id="telegram">
      <div className="mx-auto flex max-w-[1120px] flex-col items-center border-t border-atlas-border pt-20 text-center">
        <AtlasLogo className="h-24 w-24 text-atlas-accent" />
        <p className="mt-10 text-3xl font-normal tracking-[0.5em] text-atlas-text sm:text-[34px]">
          ATLAS
        </p>
        <p className="mt-7 text-xl leading-8 text-atlas-muted">
          Less chaos. More rhythm.
        </p>
        <a
          aria-label="Start ATLAS with Telegram"
          className="btn-primary mt-10"
          href="https://t.me/"
          rel="noreferrer"
          target="_blank"
        >
          Start with Telegram
        </a>
        <p className="mt-16 text-[11px] font-medium uppercase tracking-[0.75em] text-[#5D615E]">
          Personal operating system for your rhythm
        </p>
      </div>
    </footer>
  );
}
