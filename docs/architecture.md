  アーキテクチャの分類                          
                                                                                                                        
  このプロジェクトは MVC でも MVVM でもなく、以下の2つのパターンの組み合わせとして説明するのが最も適切です。            
                                                                                                                        
  1. Controller–Renderer パターン (主要な内部構造)                                                                      
                                                                                                                        
  各オーバーレイ（Marker, Polyline, Circle 等）において：

  State (MarkerState等)  →  Controller  →  Renderer  →  ネイティブ地図オブジェクト
                               ↕
                            Manager / Entity

  - State: UI状態を保持（Compose の mutableStateOf を使用）
  - Controller (OverlayControllerInterface):
  状態の差分検知（fingerprint）、追加/更新/削除の振り分け、イベントハンドリング
  - Renderer (OverlayRendererInterface): Controller から委譲され、実際のネイティブ地図オブジェクト（Google Maps の
  Marker、ArcGIS の Graphic 等）の生成・更新・削除を担当
  - Entity: State とネイティブオブジェクトを束ねるラッパー
  - Manager: Entity の保存・空間インデックス・スレッドセーフな検索

  これは厳密には Strategy パターン + Mediator パターン の組み合わせです。Renderer が
  Strategy（SDK固有の描画戦略を差し替え可能）、Controller が Mediator（State・Entity・Renderer
  間の調整役）を果たしています。

  2. Adapter / Bridge パターン (プロジェクト全体の構造)

  プロジェクト全体としては Bridge パターン が最も正確な表現です：

  抽象側 (Core)                    実装側 (各地図SDK)
  ─────────────                   ──────────────────
  MapViewControllerInterface  ←→  GoogleMapViewController
  OverlayControllerInterface  ←→  GoogleMapMarkerController
  OverlayRendererInterface    ←→  GoogleMapMarkerRenderer
                              ←→  ArcGISMarkerRenderer
                              ←→  MapboxMarkerRenderer ...

  Core モジュールが「抽象」を、各地図SDKモジュールが「実装」を提供し、両者を独立に拡張できるようになっています。

  MVC / MVVM と呼べない理由

  観点: View
  MVC/MVVM: UIコンポーネント
  このプロジェクト: Renderer がネイティブ地図オブジェクトを直接操作（Viewレイヤーが外部SDK）
  ────────────────────────────────────────
  観点: Model
  MVC/MVVM: ドメインロジック
  このプロジェクト: State は薄いデータホルダーで、ドメインロジックは Controller に内包
  ────────────────────────────────────────
  観点: データバインディング
  MVC/MVVM: MVVMの核心
  このプロジェクト: Compose の mutableStateOf は使っているが、ViewModel 層は存在しない
  ────────────────────────────────────────
  観点: Controller の役割
  MVC/MVVM: MVCではリクエスト振り分け
  このプロジェクト: ここでは状態管理 + 差分検知 + レンダリング委譲と、より広い責務を持つ

  まとめ

  一言で表現するなら：

  Bridge パターンによるSDK抽象化 + Controller–Renderer パターンによるオーバーレイ管理

  ドキュメント等で簡潔に書くなら、「Plugin-based Controller–Renderer Architecture」 や 「Bridge + Strategy
  Architecture」 と呼ぶのが実態に最も即しています。


