package androidx.activity.result.contract

import android.net.Uri

class ActivityResultContracts {
    class StartActivityForResult : ActivityResultContract<Any?, Any?>()
    class GetContent : ActivityResultContract<String, Uri?>()
    class GetMultipleContents : ActivityResultContract<String, List<Uri>>()
    class OpenDocument : ActivityResultContract<Array<String>, Uri?>()
    class OpenMultipleDocuments : ActivityResultContract<Array<String>, List<Uri>>()
    class CreateDocument(mimeType: String) : ActivityResultContract<String, Uri?>()
    class TakePicture : ActivityResultContract<Uri, Boolean>()
    class RequestPermission : ActivityResultContract<String, Boolean>()
    class RequestMultiplePermissions : ActivityResultContract<Array<String>, Map<String, Boolean>>()
    class PickVisualMedia : ActivityResultContract<PickVisualMediaRequest?, Uri?>() {
        object ImageOnly
    }
}

class PickVisualMediaRequest(val mediaType: Any? = null)
