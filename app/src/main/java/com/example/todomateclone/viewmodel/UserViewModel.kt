package com.example.todomateclone.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.todomateclone.network.RestService
import com.example.todomateclone.network.dto.*
import com.example.todomateclone.ui.user.BlockingPagingSource
import com.example.todomateclone.ui.user.FolloweePagingSource
import com.example.todomateclone.ui.user.FollowerPagingSource
import com.example.todomateclone.util.AuthStorage
import com.example.todomateclone.util.Toaster
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class UserViewModel(
    private val restService: RestService,
    private val authStorage: AuthStorage,
    private val toaster: Toaster,
) : ViewModel() {

    private val _refreshTokenValid = MutableStateFlow<Boolean?>(null)
    val refreshTokenValid: StateFlow<Boolean?> = _refreshTokenValid

    private val _searcheduser = MutableStateFlow<SearchedUserDTO?>(null)
    val searcheduser: StateFlow<SearchedUserDTO?> = _searcheduser


    private val _isfollowing = MutableStateFlow<CheckFollowResponse?>(null)
    val isfollowing: StateFlow<CheckFollowResponse?> = _isfollowing

    fun createFolloweePager(): Flow<PagingData<FolloweeDTO>> {
        return Pager(PagingConfig(pageSize = 10)) {
            FolloweePagingSource(restService)
        }.flow.cachedIn(viewModelScope)
    }

    fun createFollowerPager(): Flow<PagingData<FollowerDTO>> {
        return Pager(PagingConfig(pageSize = 10)) {
            FollowerPagingSource(restService)
        }.flow.cachedIn(viewModelScope)
    }

    fun createBlockingPager(): Flow<PagingData<BlockingDTO>> {
        return Pager(PagingConfig(pageSize = 10)) {
            BlockingPagingSource(restService)
        }.flow.cachedIn(viewModelScope)
    }

    suspend fun login(email: String, password: String) {
        try {
            val response = restService.login(LoginRequest(email, password))
            authStorage.setAuthInfo(
                response.access_token,
                response.refresh_token,
                AuthStorageUserDTO(response.user.id, response.user.email)
            )
            Log.d("UserViewModel", "authInfo is valid")
        } catch (e: HttpException) {
            if(e.code() == 400) toaster.toast("비밀번호나 아이디가 잘못되었습니다.")
            else toaster.toastApiError(e)
        }
        catch (e: Exception) {
            toaster.toastApiError(e)
        }
    }

    // Apply() writes the changes to the in-memory SharedPreferences immediately but begins an asynchronous commit to disk
    fun logout() {
        authStorage.sharedPref.edit().clear().apply()
    }

    suspend fun resendEmail(email: String) {
        try {
            restService.resendEmail(ResendEmailRequest(email))
        } catch (e: Exception) {
            toaster.toastApiError(e)
        }
    }

    suspend fun confirmCode(email: String, code: String) {
        try {
            restService.signupConfirm(SignUpConfirmRequest(email, code))
            toaster.toast("이메일 인증이 완료되었습니다.")
        } catch (e: Exception) {
            toaster.toastApiError(e)
        }
    }

    suspend fun signup(email: String, password1: String, password2: String){
        try {
            restService.signup(SignupRequest(email, password1, password2))
        } catch (e: Exception) {
            toaster.toastApiError(e)
        }
    }

    // login with kakao account
    suspend fun kakaoLogin(accessToken: String) {
        try {
            Log.d("KakaoLogin", "send idToken to server")
            val response = restService.kakaoLogin(KakaoLoginRequest(accessToken))
            authStorage.setAuthInfo(
                response.access_token,
                response.refresh_token,
                AuthStorageUserDTO(response.user.id, response.user.email)
            )
        } catch(e: Exception) {
            toaster.toastApiError(e)
        }
    }

    suspend fun deleteUser() {
        try {
            restService.deleteUser(
                id = authStorage.authInfo.value?.user!!.id
            )
        } catch (e: Exception) {
            toaster.toastApiError(e)
        }
    }

    // login with google account
    suspend fun googleLogin(code: String) {
        try {
            Log.d("GoogleLogin", "send idToken to server")
            val response = restService.googleLogin(GoogleLoginRequest(code = code))
            authStorage.setAuthInfo(
                response.access_token,
                response.refresh_token,
                AuthStorageUserDTO(response.user.id, response.user.email)
            )
        } catch(e: Exception) {
            toaster.toastApiError(e)
        }
    }

    suspend fun verifyToken(refreshToken: String) {
        try {
            Log.d("VerifyToken", "Token verification is started")
            val response = restService.verifyToken(VerifyTokenRequest(refreshToken))
            _refreshTokenValid.value = response.code() == 200
            Log.d("VerifyToken", "Token verification is completed")
        } catch(e: Exception) {
            toaster.toastApiError(e)
        }

    }

    //search user

    suspend fun searchUser(email: String) {
        viewModelScope.launch {
            try {
                _searcheduser.value = restService.searchUser(
                    email = email
                )
            } catch (e: JsonDataException) {
                _searcheduser.value=null
            }
            catch (e: Exception) {
                _searcheduser.value=null
                toaster.toastApiError(e)
            }
        }
    }

    //follow user
    suspend fun followUser(followee: Int) {
        viewModelScope.launch {
            try {
                restService.followUser(
                    FolloweeRequest(followee = followee)
                )
                toaster.toast("팔로우하였습니다.")
            } catch (e: HttpException) {
                if (e.code() == 400) {
                    toaster.toast("자기 자신은 팔로우할 수 없습니다.")
                } else {
                    toaster.toastApiError(e)
                }
            } catch (e: Exception) {
                toaster.toastApiError(e)
            }

        }
    }

    suspend fun unfollowUser(fid: Int) {
        viewModelScope.launch {
            try {
                restService.unfollowUser(
                    fid = fid
                )
                toaster.toast("언팔로우하였습니다.")
            } catch (e: Exception) {
                toaster.toastApiError(e)
            }
        }
    }

    suspend fun deleteFollower(fid: Int) {
        viewModelScope.launch {
            try {
                restService.deleteFollower(
                    fid = fid
                )
                toaster.toast("팔로워를 삭제했습니다.")
            } catch (e: Exception) {
                toaster.toastApiError(e)
            }
        }
    }

    suspend fun blockUser(follower: Int) {
        viewModelScope.launch {
            try {
                restService.blockUser(
                    FollowerRequest(follower = follower)
                )
                toaster.toast("차단했습니다.")
            } catch (e: Exception) {
                toaster.toastApiError(e)
            }
        }
    }

//    suspend fun checkFollow(fid: Int) {
//        viewModelScope.launch {
//            try {
//                _isfollowing.value = restService.checkFollow(fid = fid)
//            } catch (e: Exception) {
//                toaster.toastApiError(e)
//            }
//        }
//    }

    suspend fun unblockUser(fid: Int) {
        viewModelScope.launch {
            try {
                restService.deleteBlock(fid = fid)
                toaster.toast("차단을 해제했습니다.")
            } catch (e: Exception) {
                toaster.toastApiError(e)
            }
        }
    }





}