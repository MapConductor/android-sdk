// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightMermaid from '@pasqal-io/starlight-client-mermaid';

// https://astro.build/config
export default defineConfig({
	site: 'https://mapconductor.com',
	outDir: 'dist',
	integrations: [
		starlight({
			title: 'MapConductor',
			description: 'A unified map SDK for mobile developers',
			defaultLocale: 'root',
			locales: {
				root: {
					label: 'English',
					lang: 'en',
				},
				ja: {
					label: '日本語',
					lang: 'ja',
				},
				'es-419': {
					label: 'Español (Latinoamérica)',
					lang: 'es-419',
				},
			},
			customCss: [
				'./src/styles/custom.css',
			],
			components: {
				Head: './src/components/overrides/Head.astro',
			},
			sidebar: [
				{
					label: 'Getting Started',
					items: [
						{ label: 'Introduction', slug: 'introduction' },
						{ label: 'Installation', slug: 'installation' },
						{ label: 'Modules', slug: 'modules' },
						{ label: 'SDK Version Compatibility', slug: 'sdk-version-compatibility' },
						{ label: 'Provider Compatibility', slug: 'provider-compatibility' },
					],
				},
				{
					label: 'Setup',
					items: [
						{ label: 'Overview', slug: 'setup' },
						{ label: 'Google Maps', slug: 'setup/google-maps' },
						{ label: 'Mapbox', slug: 'setup/mapbox' },
						{ label: 'HERE Maps', slug: 'setup/here-maps' },
						{ label: 'ArcGIS', slug: 'setup/arcgis' },
						{ label: 'MapLibre', slug: 'setup/maplibre' },
					],
				},
				{
					label: 'Components',
					items: [
						{ label: 'MapView Component', slug: 'components/mapviewcomponent' },
						{ label: 'MapViewState', slug: 'components/mapviewstate' },
						{ label: 'Marker', slug: 'components/marker' },
						{ label: 'Circle', slug: 'components/circle' },
						{ label: 'Polyline', slug: 'components/polyline' },
						{ label: 'Polygon', slug: 'components/polygon' },
						{ label: 'GroundImage', slug: 'components/groundimage' },
						{ label: 'InfoBubble', slug: 'components/infobubble' },
					],
				},
				{
					label: 'Core Classes',
					items: [
						{ label: 'GeoPoint', slug: 'core/geopoint' },
						{ label: 'GeoRectBounds', slug: 'core/georectbounds' },
						{ label: 'MapCameraPosition', slug: 'core/mapcameraposition' },
						{ label: 'MapViewHolder', slug: 'core/mapviewholder' },
						{ label: 'Marker Icons', slug: 'core/marker-icons' },
						{ label: 'Marker Animation', slug: 'core/marker-animation' },
						{ label: 'Spherical Utilities', slug: 'core/spherical-utilities' },
						{ label: 'Zoom Levels', slug: 'core/zoom-levels' },
					],
				},
				{
					label: 'State Management',
					items: [
						{ label: 'Marker State', slug: 'states/marker-state' },
						{ label: 'Circle State', slug: 'states/circle-state' },
						{ label: 'Polyline State', slug: 'states/polyline-state' },
						{ label: 'Polygon State', slug: 'states/polygon-state' },
						{ label: 'GroundImage State', slug: 'states/groundimage-state' },
					],
				},
				{
					label: 'API Reference',
					items: [
						{ label: 'Initialization', slug: 'api/initialization' },
						{ label: 'Event Handlers', slug: 'api/event-handlers' },
					],
				},
				{
					label: 'Examples',
					items: [
						{ label: 'Basic Usage', slug: 'examples/basic-usage' },
						{ label: 'Advanced Usage', slug: 'examples/advanced-usage' },
					],
				},
				{
					label: 'Experimental',
					items: [
						{ label: 'Icons', slug: 'experimental/icons' },
						{ label: 'Marker Strategy', slug: 'experimental/marker-strategy' },
						{ label: 'Marker Native Strategy', slug: 'experimental/marker-native-strategy' },
					],
				},
			],
			plugins: [
				starlightMermaid({
					mermaidConfig: {
						theme: 'neutral',
						themeVariables: {
							fontSize: '16px',
							fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans JP", "Hiragino Kaku Gothic ProN", sans-serif',
						},
						flowchart: {
							htmlLabels: true,
							curve: 'basis',
							padding: 10,
						},
						sequence: {
							htmlLabels: true,
							diagramMarginX: 10,
							diagramMarginY: 10,
							boxMargin: 10,
							messageMargin: 50,
							actorFontSize: 16,
							noteFontSize: 16,
							messageFontSize: 16,
						},
						gantt: {
							htmlLabels: true,
							fontSize: 16,
						},
					},
				}),
			],
		}),
	],
});
