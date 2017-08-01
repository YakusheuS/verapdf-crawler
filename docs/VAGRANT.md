Vagrant and Ansible setup
=========================
Instructions for setting up your own instance of the Logius/veraPDF. If you want to start with a local development instance, which is advisable, you'll need VirtualBox and Vagrant to automate the management of the local development VM. To roll an instance of the application to a fresh server you only need to install Ansible and to follow the instructions.

Server Stack
------------
- Debian
- nginx
- Java 7 (for Heretrix) and Java 8 (for the application)
- Heretrix
- veraPDF

Pre-requisites
--------------

### VirtualBox
We've chosen VirtualBox, see https://www.virtualbox.org/, as the base virtualisation layer as it provides cross platform virtualisation for most standard dev setups. You can download a version for Windows, Mac OS and Linux from [here](https://www.virtualbox.org/wiki/Downloads). You'll also need the VirtualBox extension pack http://download.virtualbox.org/virtualbox/5.1.24/Oracle_VM_VirtualBox_Extension_Pack-5.1.24-117012.vbox-extpack.

### Vagrant
Vagrant, see https://www.vagrantup.com/, is used to automate the provisioning of the VirtualBox VM. It effectively creates a vanilla Debian Jessie box then uses Ansible (below) to carry out the configuration and installations necessary to set up the crawler box.

Vagrant is available for Windows, Mac OS and Linux, you can download the latest version [here](https://www.vagrantup.com/downloads.html) and follow [these installation instructions](https://www.vagrantup.com/docs/installation/).

### Ansible
Ansible, see https://www.ansible.com/get-started, is used to automate the application deployment, both to the development VM as well as any Debian server where you have SSH access. You need to install Ansible locally but it doesn't require anything installing on the target system.

Quick Starts
------------

### Vagrant
Once you have the pre-requisites installed, to start up a local Vagrant instance issue the following command from anywhere below the project root:

```bash
vagrant up
```
This will create and configure the VirtualBox and then used ansible to install the project software stack. Once that's completed you can SSH to the vagrant machine by:
```bash
vagrant ssh
```

Ansible
-------

### Getting your facts straight
As you use ansible you'll want to know what state it thinks your machine is in. Ansible tracks this information using facts and you can ask for a fact check at any time. The machine in question needs to be running and you'll need SSH access. For the development vagrant instance issue this command will do the trick from the project root:

```bash
ansible -i ansible/development --key-file=verapdf-crawler/.vagrant-wrkstn/machines/default/virtualbox/private_key -u vagrant -m setup logius.verapdf.dev
```
A quick explanation of the options:
- `-i` points to the ansible inventory file that describes the machines to be acted on, in this case `ansible/development` for the vagrant VM.
- `--keyfile` points to the private key file needed to access the machine, here we point to the vagrant machines private key file `verapdf-crawler/.vagrant-wrkstn/machines/default/virtualbox/private_key.`
- `-u` the remote ssh user, for vagrant machines we

### Task Overview

#### tersmitten.apt
Performs apt cache update and dist-update



Troubleshooting
---------------

### VirtualBox VM is unreachable by ansible SSH
Your attempt to provision the VM ends with something similar to this:

```bash
fatal: [logius.verapdf.dev]: UNREACHABLE! => {
    "changed": false,
    "msg": "Failed to connect to the host via ssh: OpenSSH_7.2p2 Ubuntu-4ubuntu2.2, OpenSSL 1.0.2g  1 Mar 2016\r\ndebug1: Reading configuration data /home/cfw/.ssh/config\r\ndebug1: Reading configuration data /etc
/ssh/ssh_config\r\ndebug1: /etc/ssh/ssh_config line 19: Applying options for *\r\ndebug1: auto-mux: Trying existing master\r\ndebug1: Control socket \"/home/cfw/.ansible/cp/2d3a7192ca\" does not exist\r\ndebug2: r
esolving \"192.168.10.10\" port 22\r\ndebug2: ssh_connect_direct: needpriv 0\r\ndebug1: Connecting to 192.168.10.10 [192.168.10.10] port 22.\r\ndebug2: fd 3 setting O_NONBLOCK\r\ndebug1: connect to address 192.168
.10.10 port 22: Connection timed out\r\nssh: connect to host 192.168.10.10 port 22: Connection timed out\r\n",
    "unreachable": true
}
```
then you might need to restart the network on your host system: `sudo ip link set up dev vboxnet0`