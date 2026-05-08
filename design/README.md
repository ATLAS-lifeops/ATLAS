# ATLAS landing design

## Figma reference

Frame URL:

```text
https://www.figma.com/design/fjgO9PSSq0Hr73t6WxXEYA/ATLAS?node-id=0-1&p=f&t=JYdlwQHRwHdJBdUy-0
```

Framelink inspection found the implemented Figma frame as:

```text
ATLAS — Premium Landing Page
Node ID: 6:2
Size: 1440 x 3500
```

## Design notes

- Premium dark graphite interface with platinum/silver accents.
- Minimal Swiss-inspired layout with large negative space.
- Geometric ATLAS logo mark built from exactly three outlined hexagons.
- Subtle abstract hexagon field in the hero background.
- Thin borders, low-contrast graphite surfaces, restrained shadow, no colorful gradients.

## Colors

| Token | Value |
|---|---|
| Background | `#0B0D10` |
| Surface | `#111418` |
| Elevated surface | `#15191F` |
| Primary text | `#F2EFE7` |
| Secondary text | `#8E929A` |
| Accent | `#D7D2C7` |
| Border | `rgba(215, 210, 199, 0.16)` |

## Typography assumptions

The Figma frame uses Inter. The frontend uses Inter when available, with `ui-sans-serif`, `system-ui`, and `sans-serif` fallbacks. No external font file is bundled for v0.3.0.

## Asset export notes

No bitmap assets are required. The ATLAS logo and hexagon details are recreated with inline SVG and CSS to keep the landing page editable and dependency-light.

## Known deviations

- The Figma MCP write limit prevented fixing the text-layer heights inside the source Figma frame; Framelink confirmed several Figma text nodes are still stored at `10px` height. The frontend implements corrected text sizing directly.
- The final Telegram bot handle is not available yet. CTA links point to the generic Telegram entry URL and should be replaced when the production bot username is known.
- The landing page uses responsive CSS/Tailwind behavior not represented in the static 1440px Figma frame.
