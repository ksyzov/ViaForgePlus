package net.aspw.viaforgeplus.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.aspw.viaforgeplus.ProtocolBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ProtocolSelector extends GuiScreen {

    private final GuiScreen parent;
    private final FinishedCallback finishedCallback;

    private SlotList list;

    public ProtocolSelector(final GuiScreen parent) {
        this(parent, (version, unused) -> ProtocolBase.getManager().setTargetVersion(version));
    }

    public ProtocolSelector(final GuiScreen parent, final FinishedCallback finishedCallback) {
        this.parent = parent;
        this.finishedCallback = finishedCallback;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(1, 4, height - 24, 68, 20, "Done"));

        list = new SlotList(mc, width, height, 0, height, 16);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        list.actionPerformed(button);

        if (button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        list.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class SlotList extends GuiSlot {

        public SlotList(Minecraft client, int width, int height, int top, int bottom, int slotHeight) {
            super(client, width, height, top, bottom, slotHeight);
        }

        @Override
        protected int getSize() {
            return ProtocolBase.versions.size();
        }

        @Override
        protected void elementClicked(int index, boolean b, int i1, int i2) {
            finishedCallback.finished(ProtocolBase.versions.get(index), parent);
        }

        @Override
        protected boolean isSelected(int index) {
            ProtocolVersion targetVersion = ProtocolBase.getManager().getTargetVersion();
            ProtocolVersion version = ProtocolBase.versions.get(index);
            return targetVersion == version;
        }

        @Override
        protected void drawBackground() {
            drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int index, int x, int y, int slotHeight, int mouseX, int mouseY) {
            ProtocolVersion targetVersion = ProtocolBase.getManager().getTargetVersion();
            ProtocolVersion version = ProtocolBase.versions.get(index);
            String text = String.format("%s%s§r", targetVersion == version ? "§a§l" : "§f", version.getName());
            drawCenteredString(mc.fontRendererObj, text, width / 2, y + 2, -1);
        }
    }

    public interface FinishedCallback {

        void finished(final ProtocolVersion version, final GuiScreen parent);

    }
}