type AtlasLogoProps = {
  className?: string;
  size?: number;
  title?: string;
};

const hexagons = [
  '50 5 89 27.5 89 72.5 50 95 11 72.5 11 27.5',
  '27 55 66 77.5 66 122.5 27 145 -12 122.5 -12 77.5',
  '73 55 112 77.5 112 122.5 73 145 34 122.5 34 77.5',
];

export function AtlasLogo({
  className,
  size = 96,
  title = 'ATLAS logo',
}: AtlasLogoProps) {
  return (
    <svg
      aria-label={title}
      className={className}
      fill="none"
      height={size}
      role="img"
      viewBox="-16 0 132 150"
      width={size}
      xmlns="http://www.w3.org/2000/svg"
    >
      {hexagons.map((points) => (
        <polygon
          key={points}
          points={points}
          stroke="currentColor"
          strokeLinejoin="round"
          strokeWidth="2"
        />
      ))}
    </svg>
  );
}
