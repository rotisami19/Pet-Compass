"""
Script pour générer les textures du Pet Compass 64x64.
Utilise l'image PetCompass.png comme base et ajoute une aiguille style vanilla qui tourne.
"""

from PIL import Image, ImageDraw
import math
import os

# Chemin de sortie
OUTPUT_DIR = r"src\main\resources\assets\petcompass\textures\item"

# Image de base (pièce dorée avec patte)
BASE_IMAGE_PATH = r"c:\Dev\Texture\PetCompass.png"

# Créer le répertoire si nécessaire
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Taille de la texture 64x64
SIZE = 64

# Couleurs de l'aiguille style vanilla Minecraft
# Partie rouge (pointe vers le pet)
NEEDLE_RED = (192, 57, 43, 255)          # Rouge vif
NEEDLE_RED_DARK = (146, 43, 33, 255)     # Rouge sombre (ombre)
NEEDLE_RED_LIGHT = (231, 76, 60, 255)    # Rouge clair (reflet)

# Partie blanche/grise (arrière de l'aiguille)
NEEDLE_WHITE = (236, 240, 241, 255)      # Blanc cassé
NEEDLE_WHITE_DARK = (189, 195, 199, 255) # Gris clair (ombre)
NEEDLE_WHITE_LIGHT = (255, 255, 255, 255) # Blanc pur (reflet)

# Centre de l'aiguille
NEEDLE_CENTER = (52, 73, 94, 255)        # Bleu-gris foncé


def load_base_image():
    """Charge l'image de base du compas (pièce dorée avec patte)."""
    img = Image.open(BASE_IMAGE_PATH).convert('RGBA')
    if img.size != (SIZE, SIZE):
        img = img.resize((SIZE, SIZE), Image.LANCZOS)
    return img


def draw_vanilla_needle(img, angle_degrees):
    """
    Dessine une aiguille style vanilla Minecraft sur l'image.
    L'aiguille est un losange allongé avec deux couleurs (rouge avant, blanc arrière).
    angle_degrees: 0 = Nord (haut), 90 = Est (droite), etc.
    """
    # Centre de l'image
    center_x, center_y = SIZE / 2, SIZE / 2
    
    # Convertir l'angle en radians (0° = haut, sens horaire)
    angle_rad = math.radians(angle_degrees - 90)
    
    # Dimensions de l'aiguille (proportionnelles à 64x64)
    needle_length = 18      # Du centre vers la pointe rouge
    needle_back_length = 10  # Du centre vers l'arrière blanc
    needle_width = 5         # Largeur au centre
    
    # Angle perpendiculaire
    perp_angle = angle_rad + math.pi / 2
    
    # Créer une copie pour dessiner
    result = img.copy()
    pixels = result.load()
    
    # Dessiner l'aiguille pixel par pixel
    for y in range(SIZE):
        for x in range(SIZE):
            dx = x - center_x
            dy = y - center_y
            dist_center = math.sqrt(dx*dx + dy*dy)
            
            # Ignorer les pixels trop loin
            if dist_center > needle_length + 2:
                continue
            
            # Projeter le point sur l'axe de l'aiguille
            dot_along = dx * math.cos(angle_rad) + dy * math.sin(angle_rad)
            dot_perp = dx * math.cos(perp_angle) + dy * math.sin(perp_angle)
            abs_dot_perp = abs(dot_perp)
            
            # Calculer la largeur autorisée à cette position
            in_needle = False
            is_front = False
            
            if dot_along >= -1 and dot_along <= needle_length:
                # Partie avant (rouge) - forme de triangle
                if dot_along >= 0:
                    progress = dot_along / needle_length
                    max_width = needle_width * (1 - progress * 0.9)
                    if abs_dot_perp <= max_width:
                        in_needle = True
                        is_front = True
                # Zone centrale
                else:
                    max_width = needle_width * (1 + dot_along / needle_back_length * 0.3)
                    if abs_dot_perp <= max_width:
                        in_needle = True
                        is_front = True
                        
            if dot_along <= 1 and dot_along >= -needle_back_length:
                # Partie arrière (blanche) - forme de triangle
                if dot_along <= 0:
                    progress = abs(dot_along) / needle_back_length
                    max_width = needle_width * (1 - progress * 0.9)
                    if abs_dot_perp <= max_width:
                        in_needle = True
                        if dot_along < -1:
                            is_front = False
            
            if in_needle:
                # Choisir la couleur selon la position
                if is_front:
                    # Partie rouge (avant)
                    if dot_perp > 1:
                        color = NEEDLE_RED_DARK   # Ombre à droite
                    elif dot_perp < -1:
                        color = NEEDLE_RED_LIGHT  # Reflet à gauche
                    else:
                        color = NEEDLE_RED
                else:
                    # Partie blanche (arrière)
                    if dot_perp > 1:
                        color = NEEDLE_WHITE_DARK
                    elif dot_perp < -1:
                        color = NEEDLE_WHITE_LIGHT
                    else:
                        color = NEEDLE_WHITE
                
                pixels[x, y] = color
    
    # Dessiner le centre de l'aiguille (petit cercle)
    center_radius = 3
    for y in range(int(center_y - center_radius - 1), int(center_y + center_radius + 2)):
        for x in range(int(center_x - center_radius - 1), int(center_x + center_radius + 2)):
            if 0 <= x < SIZE and 0 <= y < SIZE:
                dx = x - center_x
                dy = y - center_y
                dist = math.sqrt(dx*dx + dy*dy)
                if dist <= center_radius:
                    pixels[x, y] = NEEDLE_CENTER
    
    return result


def generate_all_frames():
    """Génère les 32 frames de rotation du compas."""
    
    print("Chargement de l'image de base...")
    base_img = load_base_image()
    print(f"  Image chargée: {base_img.size}")
    
    print("\nGénération des 32 textures du Pet Compass 64x64...")
    
    for i in range(32):
        # Calculer l'angle pour cette frame (32 frames = 360°)
        angle = (i * 360 / 32)
        
        # Ajouter l'aiguille avec la rotation appropriée
        img = draw_vanilla_needle(base_img, angle)
        
        # Sauvegarder
        filename = f"pet_compass_{i:02d}.png"
        filepath = os.path.join(OUTPUT_DIR, filename)
        img.save(filepath)
        print(f"  Créé: {filename} (angle: {angle:.1f}°)")
    
    print(f"\n✓ 32 textures 64x64 générées dans {OUTPUT_DIR}")


if __name__ == "__main__":
    generate_all_frames()
