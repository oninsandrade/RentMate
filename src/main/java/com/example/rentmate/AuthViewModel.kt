import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.mutableStateOf

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)


    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            isLoading.value = true
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, oldPass)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        isLoading.value = false
                        if (updateTask.isSuccessful) {
                            onResult(true, "Password updated successfully! ✅")
                        } else {
                            onResult(false, updateTask.exception?.message ?: "Update failed")
                        }
                    }
                } else {
                    isLoading.value = false
                    onResult(false, "Current password is incorrect ❌")
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}



