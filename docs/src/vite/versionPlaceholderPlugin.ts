import type { Plugin } from 'vite';
import { replaceVersions } from '../utils/versionReplacer.ts';

/**
 * Viteプラグイン: Astroコンポーネント内のバージョンプレースホルダーを置換
 * .astroファイル内の文字列（特に<pre><code>内）のプレースホルダーを置換します
 */
export default function versionPlaceholderPlugin(): Plugin {
    return {
        name: 'version-placeholder',
        apply: 'build',
        enforce: 'pre',
        transform(code: string, id: string) {
            // .astroファイルのみ処理
            if (!id.endsWith('.astro')) {
                return null;
            }

            // バージョンプレースホルダーを置換
            const transformedCode = replaceVersions(code);

            // 変更がある場合のみ結果を返す
            if (transformedCode !== code) {
                return {
                    code: transformedCode,
                    map: null,
                };
            }

            return null;
        },
    };
}
