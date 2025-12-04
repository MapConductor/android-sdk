import * as fs from 'fs';
import * as path from 'path';
import { VERSION_IDENTIFIERS } from './versionReplacer.ts';

/**
 * ビルド後のHTMLファイルを走査してバージョンプレースホルダーを置換
 * スタイル情報を含むspanタグで分割されたプレースホルダーにも対応
 */
export async function processBuiltHTML(distDir: string): Promise<void> {
    const htmlFiles: string[] = [];

    // distディレクトリ内のすべてのHTMLファイルを再帰的に取得
    function walkDir(dir: string): void {
        const files = fs.readdirSync(dir);
        for (const file of files) {
            const filePath = path.join(dir, file);
            const stat = fs.statSync(filePath);
            if (stat.isDirectory()) {
                walkDir(filePath);
            } else if (file.endsWith('.html')) {
                htmlFiles.push(filePath);
            }
        }
    }

    walkDir(distDir);

    // 各HTMLファイルを処理
    for (const filePath of htmlFiles) {
        let content = fs.readFileSync(filePath, 'utf-8');
        const originalContent = content;

        // スタイル情報を含むspanで分割されたプレースホルダーに対応
        // 複数のパターンに対応
        for (const [identifier, value] of Object.entries(VERSION_IDENTIFIERS)) {
            // HTML内では {BOM_MODULE_VERSION} のように波括弧付きで出力されることを前提とする
            const placeholder = identifier.startsWith('{') && identifier.endsWith('}')
                ? identifier
                : `{${identifier}}`;

            // プレースホルダーの中身を抽出（{BOM_MODULE_VERSION} -> BOM_MODULE_VERSION）
            const innerText = placeholder.slice(1, -1);

            // 正規表現用にメタ文字をエスケープ
            const escapedInnerText = innerText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

            // パターン1: {<span...>TEXT</span>}
            // スタイル情報付きのシンプルなパターン
            let spanPattern1 = new RegExp(
                `\\{<span[^>]*>${escapedInnerText}</span>\\}`,
                'g'
            );
            content = content.replace(spanPattern1, value);

            // パターン2: {<span...<span...>TEXT</span>...</span>} のようなネストされたスパン
            // より複雑なネスト構造に対応（最大3階層）
            let spanPattern2 = new RegExp(
                `\\{<[^>]*>(?:<[^>]*>)*${escapedInnerText}(?:</[^>]*>)*</[^>]*>\\}`,
                'g'
            );
            content = content.replace(spanPattern2, value);

            // パターン3: ${TEXT} - テンプレートリテラル形式
            // Astroコンポーネントのpropsで使用される形式
            let templateLiteralPattern = new RegExp(
                `\\\${${escapedInnerText}}`,
                'g'
            );
            content = content.replace(templateLiteralPattern, value);

            // パターン4: 通常のプレースホルダーも置換（念のため）
            content = content.replaceAll(placeholder, value);
            content = content.replaceAll(`{${placeholder}}`, value);
        }

        // 変更があった場合のみ書き込み
        if (content !== originalContent) {
            fs.writeFileSync(filePath, content, 'utf-8');
            console.log(`✓ Processed: ${filePath}`);
        }
    }
}
