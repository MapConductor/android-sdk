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
} from '../config.ts';
import { visit } from 'unist-util-visit';

export default function remarkVersionPlaceholder() {
    return (tree: any) => {
        // text: 通常の本文
        // code: ```fenced``` コードブロック
        // inlineCode: `インラインコード`
        visit(tree, ['text', 'code', 'inlineCode'], (node: any) => {
            if (typeof node.value === 'string') {
                node.value = node.value.replaceAll('{BOM_MODULE_VERSION}', BOM_MODULE_VERSION);
                node.value = node.value.replaceAll('{CORE_MODULE_VERSION}', CORE_MODULE_VERSION);
                node.value = node.value.replaceAll('{ARCGIS_MODULE_VERSION}', ARCGIS_MODULE_VERSION);
                node.value = node.value.replaceAll('{GOOGLEMAPS_MODULE_VERSION}', GOOGLEMAPS_MODULE_VERSION);
                node.value = node.value.replaceAll('{HERE_MODULE_VERSION}', HERE_MODULE_VERSION);
                node.value = node.value.replaceAll('{MAPBOX_MODULE_VERSION}', MAPBOX_MODULE_VERSION);
                node.value = node.value.replaceAll('{MAPLIBRE_MODULE_VERSION}', MAPLIBRE_MODULE_VERSION);
                node.value = node.value.replaceAll('{ICON_MODULE_VERSION}', ICON_MODULE_VERSION);
                node.value = node.value.replaceAll('{MARKER_NATIVE_STRATEGY_MODULE_VERSION}', MARKER_NATIVE_STRATEGY_MODULE_VERSION);
                node.value = node.value.replaceAll('{MARKER_STRATEGY_MODULE_VERSION}', MARKER_STRATEGY_MODULE_VERSION);
                node.value = node.value.replaceAll('{GOOGLE_MAPS_SDK_VERSION}', GOOGLE_MAPS_SDK_VERSION);
                node.value = node.value.replaceAll('{MAPBOX_SDK_VERSION}', MAPBOX_SDK_VERSION);
                node.value = node.value.replaceAll('{HERE_EXPLORER_SDK_VERSION}', HERE_EXPLORER_SDK_VERSION);
                node.value = node.value.replaceAll('{ARCGIS_SDK_VERSION}', ARCGIS_SDK_VERSION);
                node.value = node.value.replaceAll('{MAPLBRE_SDK_VERSION}', MAPLBRE_SDK_VERSION);
            }
        });
    };
}
