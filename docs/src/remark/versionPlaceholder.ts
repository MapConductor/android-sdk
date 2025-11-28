import { 
    BOM_MODULE_VERSION,
    CORE_MODULE_VERSION,
    ARCGIS_MODULE_VERSION,
    GOOGLEMAPS_MODULE_VERSION,
    HERE_MODULE_VERSION,
    MAPBOX_MODULE_VERSION,
    MAPLIBRE_MODULE_VERSION,
    ICON_MODULE_VERSION,
    MARKER_NATIVE_STRATEGY_MODULE_VERSION,
    MARKER_STRATEGY_MODULE_VERSION,
    GOOGLE_MAPS_SDK_VERSION,
    MAPBOX_SDK_VERSION,
    HERE_EXPLORER_SDK_VERSION,
    ARCGIS_SDK_VERSION,
    MAPLBRE_SDK_VERSION,
    KOTLIN_VERSION,
    ANDROID_MIN_SDK_VERSION,
    ANDROID_TARGET_SDK_VERSION,
    JETPACK_COMPOSE_VERSION,
    JAVA_VERSION,
} from '../config.ts';
import { visit } from 'unist-util-visit';

function replaceVersions(str: string): string {
    return str
        .replaceAll('{BOM_MODULE_VERSION}', BOM_MODULE_VERSION)
        .replaceAll('{CORE_MODULE_VERSION}', CORE_MODULE_VERSION)
        .replaceAll('{ARCGIS_MODULE_VERSION}', ARCGIS_MODULE_VERSION)
        .replaceAll('{GOOGLEMAPS_MODULE_VERSION}', GOOGLEMAPS_MODULE_VERSION)
        .replaceAll('{HERE_MODULE_VERSION}', HERE_MODULE_VERSION)
        .replaceAll('{MAPBOX_MODULE_VERSION}', MAPBOX_MODULE_VERSION)
        .replaceAll('{MAPLIBRE_MODULE_VERSION}', MAPLIBRE_MODULE_VERSION)
        .replaceAll('{ICON_MODULE_VERSION}', ICON_MODULE_VERSION)
        .replaceAll('{MARKER_NATIVE_STRATEGY_MODULE_VERSION}', MARKER_NATIVE_STRATEGY_MODULE_VERSION)
        .replaceAll('{MARKER_STRATEGY_MODULE_VERSION}', MARKER_STRATEGY_MODULE_VERSION)
        .replaceAll('{GOOGLE_MAPS_SDK_VERSION}', GOOGLE_MAPS_SDK_VERSION)
        .replaceAll('{MAPBOX_SDK_VERSION}', MAPBOX_SDK_VERSION)
        .replaceAll('{HERE_EXPLORER_SDK_VERSION}', HERE_EXPLORER_SDK_VERSION)
        .replaceAll('{ARCGIS_SDK_VERSION}', ARCGIS_SDK_VERSION)
        .replaceAll('{MAPLBRE_SDK_VERSION}', MAPLBRE_SDK_VERSION)
        .replaceAll('{KOTLIN_VERSION}', KOTLIN_VERSION)
        .replaceAll('{ANDROID_MIN_SDK_VERSION}', ANDROID_MIN_SDK_VERSION)
        .replaceAll('{ANDROID_TARGET_SDK_VERSION}', ANDROID_TARGET_SDK_VERSION)
        .replaceAll('{JETPACK_COMPOSE_VERSION}', JETPACK_COMPOSE_VERSION)
        .replaceAll('{JAVA_VERSION}', JAVA_VERSION);
}

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
                    const replacements: Record<string, string> = {
                        'BOM_MODULE_VERSION': BOM_MODULE_VERSION,
                        'CORE_MODULE_VERSION': CORE_MODULE_VERSION,
                        'ARCGIS_MODULE_VERSION': ARCGIS_MODULE_VERSION,
                        'GOOGLEMAPS_MODULE_VERSION': GOOGLEMAPS_MODULE_VERSION,
                        'HERE_MODULE_VERSION': HERE_MODULE_VERSION,
                        'MAPBOX_MODULE_VERSION': MAPBOX_MODULE_VERSION,
                        'MAPLIBRE_MODULE_VERSION': MAPLIBRE_MODULE_VERSION,
                        'ICON_MODULE_VERSION': ICON_MODULE_VERSION,
                        'MARKER_NATIVE_STRATEGY_MODULE_VERSION': MARKER_NATIVE_STRATEGY_MODULE_VERSION,
                        'MARKER_STRATEGY_MODULE_VERSION': MARKER_STRATEGY_MODULE_VERSION,
                        'GOOGLE_MAPS_SDK_VERSION': GOOGLE_MAPS_SDK_VERSION,
                        'MAPBOX_SDK_VERSION': MAPBOX_SDK_VERSION,
                        'HERE_EXPLORER_SDK_VERSION': HERE_EXPLORER_SDK_VERSION,
                        'ARCGIS_SDK_VERSION': ARCGIS_SDK_VERSION,
                        'MAPLBRE_SDK_VERSION': MAPLBRE_SDK_VERSION,
                        'KOTLIN_VERSION': KOTLIN_VERSION,
                        'ANDROID_MIN_SDK_VERSION': ANDROID_MIN_SDK_VERSION,
                        'ANDROID_TARGET_SDK_VERSION': ANDROID_TARGET_SDK_VERSION,
                        'JETPACK_COMPOSE_VERSION': JETPACK_COMPOSE_VERSION,
                        'JAVA_VERSION': JAVA_VERSION,
                    };

                    if (replacements[name]) {
                        // MDX式ノードをテキストノードに変換
                        node.type = 'text';
                        node.value = replacements[name];
                        delete node.data;
                    }
                }
            }
        });
    };
}
