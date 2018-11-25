//服务层
app.service('loginService',function($http){

    this.findLoginUser=function(){
        return $http.get('../login/findLoginUser.do');
    }
});
