"""Genera un ZIP del proyecto y sus capturas para abrirlo en Android Studio."""
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

proyecto = Path(__file__).resolve().parent
salida = proyecto.parent / "entrega" / "Exp2_S5_Cristian_Nahuas_PMY2201.zip"
salida.parent.mkdir(exist_ok=True)
excluir_directorios = {"build", ".gradle", ".kotlin", ".idea", ".vscode", ".git", "__pycache__"}
excluir_archivos = {"local.properties", ".DS_Store"}

with ZipFile(salida, "w", ZIP_DEFLATED) as archivo:
    for ruta in sorted(proyecto.rglob("*")):
        relativa = ruta.relative_to(proyecto)
        if not ruta.is_file() or excluir_directorios.intersection(relativa.parts):
            continue
        if ruta.name in excluir_archivos or ruta.suffix in {".iml", ".zip", ".apk", ".aab"}:
            continue
        archivo.write(ruta, Path("VeterinariaApp") / relativa)

with ZipFile(salida) as archivo:
    assert archivo.testzip() is None, "El ZIP no superó la comprobación de integridad."
    for ruta in ("app/src/main/AndroidManifest.xml", "gradle/wrapper/gradle-wrapper.jar", "datos/build.gradle.kts", "utilidades/build.gradle.kts"):
        assert f"VeterinariaApp/{ruta}" in archivo.namelist(), f"Falta {ruta}"

print(salida)
