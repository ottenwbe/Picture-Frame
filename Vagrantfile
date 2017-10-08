# -*- mode: ruby -*-
# vi: set ft=ruby :

$script = <<SCRIPT
echo Building Docker Image
cd /vagrant
sudo apt update
sudo apt -y upgrade
sudo apt -y install docker.io
sudo docker build . -t image-frame/latest
SCRIPT

Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/xenial64"

  config.vm.provision "shell", inline: $script

end
