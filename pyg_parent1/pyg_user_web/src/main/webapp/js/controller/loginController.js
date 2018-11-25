 //控制层 
app.controller('loginController' ,function($scope,$controller   ,loginService){
	$scope.findLoginUser=function(){
		loginService.findLoginUser().success(
			function(response){
				$scope.loginName=response.loginName;
			}
		)
	}
});	
