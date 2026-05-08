const steps = [
  {
    number: '01',
    title: 'Check in',
    copy: 'Send current state, constraints, energy, schedule, and priorities.',
  },
  {
    number: '02',
    title: 'Get a realistic plan',
    copy: 'Receive a structured day or week across training, recovery, habits, and meals.',
  },
  {
    number: '03',
    title: 'Adjust through the week',
    copy: 'ATLAS adapts plans as fatigue, events, and momentum change.',
  },
];

export function HowItWorks() {
  return (
    <section className="section-shell border-b border-atlas-border">
      <div className="max-w-3xl">
        <p className="section-kicker">02 / How it works</p>
        <h2 className="mt-9 text-3xl font-normal leading-tight text-atlas-text sm:text-[38px]">
          A weekly rhythm, adjusted in real time
        </h2>
      </div>

      <div className="mt-20 grid gap-5 lg:grid-cols-3">
        {steps.map((step, index) => (
          <article className="atlas-card relative min-h-[168px]" key={step.title}>
            <div className="flex items-start justify-between">
              <span className="text-xs font-medium tracking-[0.75em] text-atlas-muted">
                {step.number}
              </span>
              <span aria-hidden="true" className="mini-outline-hex" />
            </div>
            <h3 className="mt-7 text-[22px] font-normal leading-8 text-atlas-text">
              {step.title}
            </h3>
            <p className="mt-3 text-sm leading-6 text-atlas-muted">
              {step.copy}
            </p>
            {index < steps.length - 1 ? (
              <span
                aria-hidden="true"
                className="absolute -right-[22px] top-1/2 hidden h-px w-10 bg-atlas-line lg:block"
              />
            ) : null}
          </article>
        ))}
      </div>
    </section>
  );
}
