var app = angular.module('ImageApp', []);

app.controller('RandomImageCtrl', function($scope) {
    $scope.randomImageUrl = "/images/rnd-image";
});