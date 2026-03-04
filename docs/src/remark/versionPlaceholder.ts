import { visit } from 'unist-util-visit';
import { replaceVersions, VERSION_IDENTIFIERS } from '../utils/versionReplacer.ts';

export default function remarkVersionPlaceholder() {
    return (tree: any) => {
        // すべてのノードを訪問して、文字列値を持つプロパティを置換
        visit(tree, (node: any) => {
            // value プロパティを持つノード (text, code, inlineCode など)
            if (typeof node.value === 'string') {
                node.value = replaceVersions(node.value);
            }

            // MDX式ノードの場合、data.estree構造を確認
            if (node.type === 'mdxTextExpression' || node.type === 'mdxFlowExpression') {
                // 式の内容を文字列として処理
                if (node.value && typeof node.value === 'string') {
                    node.value = replaceVersions(node.value);
                }
                // data.estree.body[0].expression を確認（識別子の場合）
                if (node.data?.estree?.body?.[0]?.expression?.name) {
                    const name = node.data.estree.body[0].expression.name;

                    if (VERSION_IDENTIFIERS[name]) {
                        // MDX式ノードをテキストノードに変換
                        node.type = 'text';
                        node.value = VERSION_IDENTIFIERS[name];
                        delete node.data;
                    }
                }
            }
        });
    };
}
