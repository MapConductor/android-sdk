// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightMermaid from '@pasqal-io/starlight-client-mermaid';
import { fileURLToPath } from 'node:url';
import remarkVersionPlaceholder from './src/remark/versionPlaceholder.ts';

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
			social: [
				{ icon: 'github', label: 'GitHub', href: 'https://github.com/withastro/starlight' },
			],
			sidebar: [
				{
					label: 'Getting Started',
					translations: {
						ja: 'はじめに',
					},
					items: [
						{ slug: 'introduction' },
						{ slug: 'get-started' },
						{ slug: 'modules' },
						{ slug: 'sdk-version-compatibility' },
						{ slug: 'provider-compatibility' },
					],
				},
				{
					label: 'Setup',
					translations: {
						ja: 'セットアップ',
					},
					items: [
						{ slug: 'setup' },
						{ slug: 'setup/google-maps' },
						{ slug: 'setup/mapbox' },
						{ slug: 'setup/here-maps' },
						{ slug: 'setup/arcgis' },
						{ slug: 'setup/maplibre' },
					],
				},
				{
					label: 'Components',
					translations: {
						ja: 'コンポーネント',
					},
					items: [
						{ slug: 'components/mapviewcomponent' },
						{ slug: 'components/mapviewstate' },
						{ slug: 'components/marker' },
						{ slug: 'components/circle' },
						{ slug: 'components/polyline' },
						{ slug: 'components/polygon' },
						{ slug: 'components/groundimage' },
						{ slug: 'components/infobubble' },
					],
				},
				{
					label: 'Core Classes',
					translations: {
						ja: 'コアクラス',
					},
					items: [
						{ slug: 'core/geopoint' },
						{ slug: 'core/georectbounds' },
						{ slug: 'core/mapcameraposition' },
						{ slug: 'core/mapviewholder' },
						{ slug: 'core/marker-icons' },
						{ slug: 'core/marker-animation' },
						{ slug: 'core/spherical-utilities' },
						{ slug: 'core/zoom-levels' },
					],
				},
				{
					label: 'State Management',
					translations: {
						ja: 'ステート管理',
					},
					items: [
						{ slug: 'states/marker-state' },
						{ slug: 'states/circle-state' },
						{ slug: 'states/polyline-state' },
						{ slug: 'states/polygon-state' },
						{ slug: 'states/groundimage-state' },
					],
				},
				{
					label: 'API Reference',
					translations: {
						ja: 'API リファレンス',
					},
					items: [
						{ slug: 'api/initialization' },
						{ slug: 'api/event-handlers' },
					],
				},
				{
					label: 'Examples',
					translations: {
						ja: 'サンプル',
					},
					items: [
						{ slug: 'examples/basic-usage' },
						{ slug: 'examples/advanced-usage' },
					],
				},
				{
					label: 'Experimental',
					translations: {
						ja: '実験的機能',
					},
					items: [
						{ slug: 'experimental/icons' },
						{ slug: 'experimental/marker-strategy' },
						{ slug: 'experimental/marker-native-strategy' },
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
	vite: {
		resolve: {
			// Match Starlight docs behavior so `~/` points to `src/`
			alias: {
				'~': fileURLToPath(new URL('./src', import.meta.url)),
			},
		},
	},
	markdown: {
		remarkPlugins: [remarkVersionPlaceholder],
	},
	mdx: {
		remarkPlugins: [remarkVersionPlaceholder],
	},
});
