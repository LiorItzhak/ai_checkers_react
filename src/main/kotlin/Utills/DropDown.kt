//package Utills
//
//import org.w3c.dom.events.Event
//import react.*
//
//@JsName("default")
//external class Dropdown : Component<DropdownProps, RState> {
//    override fun render(): ReactElement?
//}
//
//
//// Dropdown
//interface DropdownProps : RDynamicProps {
//    var anchor: String //fixme could be a function
//    var initShown: Boolean
//    var activeClassName: String
//    var clickMode: Boolean
//    var hoverMode: Boolean
//    var hoverShowTimeOut: Int
//    var hoverHideTimeOut: Int
//    var onShow: REventFunc
//    var onHide: REventFunc
////    var onMouseEnter: REventFunc
////    var onMouseLeave: REventFunc
//}
//
//fun RBuilder.dropdown(block: RHandler<DropdownProps> = {}) = child(Dropdown::class, block)
//
//
//
//interface RClassnameProps : RProps {
//    var className: String
//}
//
//typealias REventFunc = (Event) -> Unit
//
//interface RDynamicProps : RProps {
//    var onCopy: REventFunc
//    var onCut: REventFunc
//    var onPaste: REventFunc
//    var onCompositionEnd: REventFunc
//    var onCompositionStart: REventFunc
//    var onCompositionUpdate: REventFunc
//    var onKeyDown: REventFunc
//    var onKeyPress: REventFunc
//    var onKeyUp: REventFunc
//    var onFocus: REventFunc
//    var onBlur: REventFunc
//    var onChange: REventFunc
//    var onInput: REventFunc
//    var onSubmit: REventFunc
//    var onClick: REventFunc
//    var onContextMenu: REventFunc
//    var onDoubleClick: REventFunc
//    var onDrag: REventFunc
//    var onDragEnd: REventFunc
//    var onDragEnter: REventFunc
//    var onDragExit: REventFunc
//    var onDragLeave: REventFunc
//    var onDragOver: REventFunc
//    var onDragStart: REventFunc
//    var onDrop: REventFunc
//    var onMouseDown: REventFunc
//    var onMouseEnter: REventFunc
//    var onMouseLeave: REventFunc
//    var onMouseMove: REventFunc
//    var onMouseOut: REventFunc
//    var onMouseOver: REventFunc
//    var onMouseUp: REventFunc
//    var onSelect: REventFunc
//    var onTouchCancel: REventFunc
//    var onTouchEnd: REventFunc
//    var onTouchMove: REventFunc
//    var onTouchStart: REventFunc
//    var onScroll: REventFunc
//    var onWheel: REventFunc
//    var onAbort: REventFunc
//    var onCanPlay: REventFunc
//    var onCanPlayThrough: REventFunc
//    var onDurationChange: REventFunc
//    var onEmptied: REventFunc
//    var onEncrypted: REventFunc
//    var onEnded: REventFunc
//    var onError: REventFunc
//    var onLoadedData: REventFunc
//    var onLoadedMetadata: REventFunc
//    var onLoadStart: REventFunc
//    var onPause: REventFunc
//    var onPlay: REventFunc
//    var onPlaying: REventFunc
//    var onProgress: REventFunc
//    var onRateChange: REventFunc
//    var onSeeked: REventFunc
//    var onSeeking: REventFunc
//    var onStalled: REventFunc
//    var onSuspend: REventFunc
//    var onTimeUpdate: REventFunc
//    var onVolumeChange: REventFunc
//    var onWaiting: REventFunc
//    var onLoad: REventFunc
//    //var onError: REventFunc
//    var onAnimationStart: REventFunc
//    var onAnimationEnd: REventFunc
//    var onAnimationIteration: REventFunc
//    var onTransitionEnd: REventFunc
//
//    // HTML attributes:String
//    var accept: String
//    var acceptCharset: String
//    var accessKey: String
//    var action: String
//    var allowFullScreen: String
//    var allowTransparency: String
//    var alt: String
//    var async: String
//    var autoComplete: String
//    var autoFocus: String
//    var autoPlay: String
//    var capture: String
//    var cellPadding: String
//    var cellSpacing: String
//    var challenge: String
//    var charSet: String
//    var checked: String
//    var cite: String
//    var classID: String
//    var className: String
//    var colSpan: String
//    var cols: String
//    var content: String
//    var contentEditable: String
//    var contextMenu: String
//    var controls: String
//    var coords: String
//    var crossOrigin: String
//    //    var data: String
//    var dateTime: String
//    var default: String
//    var defer: String
//    var dir: String
//    var disabled: String
//    var download: String
//    var draggable: String
//    var encType: String
//    var form: String
//    var formAction: String
//    var formEncType: String
//    var formMethod: String
//    var formNoValidate: String
//    var formTarget: String
//    var frameBorder: String
//    var headers: String
//    var height: String
//    var hidden: String
//    var high: String
//    var href: String
//    var hrefLang: String
//    var htmlFor: String
//    var httpEquiv: String
//    var icon: String
//    var id: String
//    var inputMode: String
//    var integrity: String
//    var `is`: String
//    var keyParams: String
//    var keyType: String
//    var kind: String
//    var label: String
//    var lang: String
//    var list: String
//    var loop: String
//    var low: String
//    var manifest: String
//    var marginHeight: String
//    var marginWidth: String
//    var max: String
//    var maxLength: String
//    var media: String
//    var mediaGroup: String
//    var method: String
//    var min: String
//    var minLength: String
//    var multiple: String
//    var muted: String
//    var name: String
//    var noValidate: String
//    var nonce: String
//    var open: String
//    var optimum: String
//    var pattern: String
//    var placeholder: String
//    var poster: String
//    var preload: String
//    var profile: String
//    var radioGroup: String
//    var readOnly: String
//    var rel: String
//    var required: String
//    var reversed: String
//    var role: String
//    var rowSpan: String
//    var rows: String
//    var sandbox: String
//    var scope: String
//    var scoped: String
//    var scrolling: String
//    var seamless: String
//    var selected: String
//    var shape: String
//    var size: String
//    var sizes: String
//    var span: String
//    var spellCheck: String
//    var src: String
//    var srcDoc: String
//    var srcLang: String
//    var srcSet: String
//    var start: String
//    var step: String
//    var style: String
//    var summary: String
//    var tabIndex: String
//    var target: String
//    var title: String
//    var type: String
//    var useMap: String
//    var value: String
//    var width: String
//    var wmode: String
//    var wrap: String
//}