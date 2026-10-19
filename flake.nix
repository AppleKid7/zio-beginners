{
  description = "Development environment for zio-beginners";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  };

  outputs =
    { nixpkgs, ... }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forAllSystems = nixpkgs.lib.genAttrs systems;
    in
    {
      devShells = forAllSystems (
        system:
        let
          pkgs = import nixpkgs { inherit system; };
          jdk = pkgs.jdk21;
        in
        {
          default = pkgs.mkShell {
            packages = [
              jdk
              pkgs.sbt
              pkgs.scalafmt
              pkgs.coursier
              pkgs.metals
              pkgs.scala-cli
            ];

            JAVA_HOME = jdk;
          };
        }
      );
    };
}
