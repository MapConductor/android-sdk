import type { AstroIntegration } from 'astro';
import { fileURLToPath } from 'node:url';
import { processBuiltHTML } from '../utils/postBuildProcessor.ts';

/**
 * Astro Integration: ビルド完了後のHTMLポスト処理
 * バージョンプレースホルダーをすべてのHTMLファイルで置換
 */
export default function postBuildIntegration(): AstroIntegration {
    return {
        name: 'post-build-processor',
        hooks: {
            'astro:build:done': async ({ dir }) => {
                console.log('\n🔄 Post-processing HTML files for version placeholders...');
                try {
                    const distPath = fileURLToPath(dir);
                    await processBuiltHTML(distPath);
                    console.log('✅ Version placeholder processing complete!\n');
                } catch (error) {
                    console.error('❌ Error during post-build processing:', error);
                    throw error;
                }
            },
        },
    };
}
